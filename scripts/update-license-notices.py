#!/usr/bin/env python3
"""Run from the repository root after exporting the runtime index. Review license changes before delivery."""
import pathlib,json,zipfile,io,urllib.request,xml.etree.ElementTree as E
root=pathlib.Path('app/src/main/assets/licenses');root.mkdir(parents=True,exist_ok=True)
def download(url):
 return urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Gass-license-review/2.2.9'}),timeout=40).read().decode('utf-8')
urls={
'Apache_2.0':'https://www.apache.org/licenses/LICENSE-2.0.txt',
'MapLibre_Native_y_componentes':'https://raw.githubusercontent.com/maplibre/maplibre-native/android-v13.7.1/platform/android/LICENSE.md',
'MapLibre_Java':'https://raw.githubusercontent.com/maplibre/maplibre-java/main/LICENSE',
'MapLibre_Gestures':'https://raw.githubusercontent.com/maplibre/maplibre-gestures-android/main/LICENSE.md',
'Kotlin':'https://raw.githubusercontent.com/JetBrains/kotlin/v2.2.10/license/LICENSE.txt',
'Kotlin_Coroutines':'https://raw.githubusercontent.com/Kotlin/kotlinx.coroutines/1.10.2/LICENSE.txt',
'OkHttp':'https://raw.githubusercontent.com/square/okhttp/parent-4.12.0/LICENSE.txt',
'Okio':'https://raw.githubusercontent.com/square/okio/parent-3.6.0/LICENSE.txt',
'Retrofit':'https://raw.githubusercontent.com/square/retrofit/2.11.0/LICENSE.txt',
'Gson':'https://raw.githubusercontent.com/google/gson/gson-parent-2.11.0/LICENSE',
'Timber':'https://raw.githubusercontent.com/JakeWharton/timber/5.0.1/LICENSE.txt',
'Guava_ListenableFuture':'https://raw.githubusercontent.com/google/guava/v33.4.8/LICENSE',
'AndroidX':'https://raw.githubusercontent.com/androidx/androidx/androidx-main/LICENSE.txt',
}
for name,url in urls.items():
 if (root/(name+'.txt')).exists(): continue
 try:
  text=download(url);(root/(name+'.txt')).write_text('Fuente: '+url+'\n\n'+text);print(name,len(text))
 except Exception as e: raise RuntimeError('Missing license: '+name+' from '+url) from e
(root/'Gass_GPLv3.txt').write_text(pathlib.Path('LICENSE').read_text())
artifacts=json.loads(pathlib.Path('app/build/reports/runtime-license-index.json').read_text());inventory=[];embedded=[]
cache=pathlib.Path.home()/'.gradle/caches/modules-2/files-2.1'
for a in artifacts:
 coordinate=f"{a['group']}:{a['name']}:{a['version']}"
 poms=list((cache/a['group']/a['name']/a['version']).glob('*/*.pom'))
 info=[]
 for pom in poms:
  doc=E.fromstring(pom.read_text());ns={'m':'http://maven.apache.org/POM/4.0.0'}
  info += [(x.findtext('m:name',default='',namespaces=ns), x.findtext('m:url',default='',namespaces=ns)) for x in doc.findall('m:licenses/m:license',ns)]
 inventory.append(coordinate+'\n'+('\n'.join(f'{n} · {u}' for n,u in info) or 'Avisos incluidos en los textos de su familia y en los avisos del binario.'))
 def extract(z):
  for n in z.namelist():
   if any(t in n.lower().split('/')[-1] for t in ['license','notice','copying','copyright']) and not n.endswith('/') and z.getinfo(n).file_size<1000000:
    try: embedded.append(coordinate+' / '+n+'\n'+z.read(n).decode('utf-8'))
    except UnicodeError: pass
 with zipfile.ZipFile(a['file']) as z:
  extract(z)
  if 'classes.jar' in z.namelist():
   with zipfile.ZipFile(io.BytesIO(z.read('classes.jar'))) as nested: extract(nested)
(root/'Inventario_de_bibliotecas.txt').write_text('Bibliotecas de ejecución resueltas para Gass\n\n'+'\n\n'.join(inventory))
(root/'Avisos_incluidos_en_los_binarios.txt').write_text('\n\n====================\n\n'.join(embedded) or 'No se incluyen avisos adicionales en los archivos inspeccionados.')
(root/'Google_Play_Services.txt').write_text('Google Play services Location, Base, Basement y Tasks\nCopyright Google LLC.\nComponentes propietarios sujetos a las condiciones del SDK de Google y Google APIs.\nhttps://developer.android.com/studio/terms\nhttps://developers.google.com/terms\nhttps://developers.google.com/android/guides/opensource\nLos avisos de código abierto incorporados a los binarios se reproducen en «Avisos incluidos en los binarios». No se les atribuye una licencia Apache general a los componentes propietarios.\n')
pathlib.Path('app/src/main/assets/license-runtime.json').write_text(json.dumps(sorted({a['group']+':'+a['name']+':'+a['version'] for a in artifacts}), indent=2)+'\n')
print('artifacts',len(artifacts),'embedded notices',len(embedded))
