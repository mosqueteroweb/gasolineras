import io
from contextlib import redirect_stdout
from pathlib import Path
import sys
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import release_preflight as preflight
import publish_release as publisher

SHA = 'a' * 40
OTHER_SHA = 'b' * 40


def run_record(run_id=10, **changes):
    data = {'id': run_id, 'head_sha': SHA, 'head_branch': 'main', 'event': 'push',
            'repository': {'full_name': preflight.REPOSITORY}, 'status': 'completed', 'conclusion': 'success'}
    data.update(changes)
    return data


def job_record(**changes):
    data = {'name': 'validate', 'conclusion': 'success', 'steps': [
        {'name': 'Test release guards', 'conclusion': 'success'},
        {'name': 'Clean Linux dependency verification and build', 'conclusion': 'success'}]}
    data.update(changes)
    return data


class PreflightTests(unittest.TestCase):
    def status(self, runs, jobs=None):
        def get(path):
            if '/jobs?' in path:
                return {'jobs': [job_record()] if jobs is None else jobs}
            self.assertIn('head_sha=' + SHA, path)
            self.assertIn('branch=main', path)
            return {'workflow_runs': runs}
        return preflight.preflight_status(SHA, get)[0]

    def test_accepts_successful_cold_main_run_for_exact_commit(self):
        self.assertEqual('success', self.status([run_record()]))

    def test_rejects_different_commit_branch_event_or_repository(self):
        for change in [{'head_sha': OTHER_SHA}, {'head_branch': 'v2.2.9'}, {'event': 'pull_request'},
                       {'repository': {'full_name': 'someone/fork'}}]:
            with self.subTest(change=change):
                self.assertEqual('pending', self.status([run_record(**change)]))

    def test_does_not_use_old_success_when_latest_run_failed(self):
        self.assertEqual('failure', self.status([run_record(), run_record(11, conclusion='failure')]))

    def test_waits_for_latest_in_progress_run_even_if_older_success_exists(self):
        self.assertEqual('pending', self.status([run_record(), run_record(11, status='in_progress', conclusion=None)]))

    def test_missing_skipped_or_failed_validation_job_blocks_publication(self):
        for jobs in [[], [job_record(conclusion='skipped')], [job_record(conclusion='failure')]]:
            with self.subTest(jobs=jobs):
                self.assertEqual('failure', self.status([run_record()], jobs))

    def test_old_validation_without_cold_step_is_not_accepted(self):
        self.assertEqual('failure', self.status([run_record()], [job_record(steps=[])]))

    def test_skipped_cold_step_is_not_accepted(self):
        job = job_record()
        job['steps'][1]['conclusion'] = 'skipped'
        self.assertEqual('failure', self.status([run_record()], [job]))

    def test_missing_run_and_zero_timeout_fails_without_sleeping(self):
        with patch.object(preflight.time, 'sleep') as sleep:
            with self.assertRaises(preflight.PreflightError):
                preflight.wait_for_preflight(SHA, timeout=0, get=lambda path: {'workflow_runs': []})
            sleep.assert_not_called()

    def test_malformed_sha_never_makes_http_request(self):
        with self.assertRaises(preflight.PreflightError):
            preflight.preflight_status('main', lambda path: self.fail('unexpected request'))


class PublicationTests(unittest.TestCase):
    def execute(self, *, failure=False, check_only=False, remote_changed=False):
        events = []

        def git(*args, **kwargs):
            events.append(('git', args))
            return 0, SHA if args == ('rev-parse', 'HEAD') else ''

        def validated(*args, **kwargs):
            events.append(('validated', SHA))
            if failure:
                raise preflight.PreflightError('checksum missing')
            return 'https://github.com/example/run'

        remote_calls = 0
        def remote(*args):
            nonlocal remote_calls
            remote_calls += 1
            if remote_changed and remote_calls == 2:
                raise preflight.PreflightError('main changed')

        with patch.object(publisher, 'git', side_effect=git), \
             patch.object(publisher, 'require_checkout'), \
             patch.object(publisher, 'require_remote_commit', side_effect=remote), \
             patch.object(publisher, 'require_new_tag'), \
             patch.object(publisher, 'wait_for_preflight', side_effect=validated):
            try:
                with redirect_stdout(io.StringIO()):
                    publisher.publish(check_only=check_only, timeout=0)
            except preflight.PreflightError:
                if not (failure or remote_changed):
                    raise
        return events

    def test_failure_never_creates_or_pushes_tag(self):
        events = self.execute(failure=True)
        self.assertFalse(any(e[0] == 'git' and 'tag' in e[1] for e in events))
        self.assertFalse(any(e[0] == 'git' and any('refs/tags/' in a for a in e[1]) for e in events))

    def test_new_tag_is_created_only_after_successful_exact_commit_validation(self):
        events = self.execute()
        validated = next(i for i, e in enumerate(events) if e[0] == 'validated')
        tagged = next(i for i, e in enumerate(events) if e[0] == 'git' and 'tag' in e[1])
        self.assertGreater(tagged, validated)
        self.assertEqual(SHA, events[tagged][1][-1])
        self.assertFalse(any('--force' in a for e in events if e[0] == 'git' for a in e[1]))

    def test_check_only_does_not_push_or_create_tags(self):
        events = self.execute(check_only=True)
        self.assertFalse(any(e[0] == 'git' and any(a in ['push', 'tag'] for a in e[1]) for e in events))

    def test_remote_change_after_validation_blocks_tag_creation(self):
        events = self.execute(remote_changed=True)
        self.assertFalse(any(e[0] == 'git' and 'tag' in e[1] for e in events))

    def test_existing_local_tag_is_not_replaced(self):
        with patch.object(publisher, 'git', return_value=(0, '')) as git:
            with self.assertRaises(preflight.PreflightError):
                publisher.require_new_tag('v2.2.9')
            self.assertEqual(1, git.call_count)

    def test_existing_remote_tag_is_not_replaced(self):
        with patch.object(publisher, 'git', side_effect=[(1, ''), (0, SHA)]):
            with self.assertRaises(preflight.PreflightError):
                publisher.require_new_tag('v2.2.9')

    def test_dirty_checkout_is_not_published(self):
        with patch.object(publisher, 'git', return_value=(0, ' M app/build.gradle.kts')):
            with self.assertRaises(preflight.PreflightError):
                publisher.require_checkout(SHA)

    def test_git_error_redacts_remote_authentication(self):
        text = publisher.redact('https://fake-user:fake-password@github.com/repo ghp_exampleplaceholder')
        self.assertNotIn('fake-password', text)
        self.assertNotIn('ghp_exampleplaceholder', text)


if __name__ == '__main__':
    unittest.main()
