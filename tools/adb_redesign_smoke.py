#!/usr/bin/env python3
"""Smoke-test the isolated preview app without touching the published app's session."""
import argparse
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument('--device')
parser.add_argument('--output', default='/tmp/meduza-redesign-smoke')
args = parser.parse_args()
base = ['adb'] + (['-s', args.device] if args.device else [])
package = 'uz.meduza.anime.preview'
out = Path(args.output); out.mkdir(parents=True, exist_ok=True)

def adb(*parts, binary=False):
    return subprocess.check_output(base + list(parts), text=not binary, stderr=subprocess.DEVNULL)

def snapshot():
    result=adb('shell', 'uiautomator', 'dump', '/sdcard/meduza-redesign-qa.xml')
    if 'dumped to:' not in result:
        return ET.Element('hierarchy')
    raw=adb('exec-out', 'cat', '/sdcard/meduza-redesign-qa.xml')
    try:
        return ET.fromstring(raw)
    except ET.ParseError:
        return ET.Element('hierarchy')  # Android can still be starting the accessibility service.

def match(node, label):
    return node.get('text') == label or node.get('content-desc') == label

def wait(label, timeout=30):
    deadline=time.monotonic()+timeout
    while time.monotonic()<deadline:
        root=snapshot()
        for node in root.iter('node'):
            if match(node,label): return node
        time.sleep(.5)
    raise AssertionError('UI element missing: '+label)

def tap(label):
    node=wait(label)
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.get('bounds')))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))

def save(name):
    time.sleep(.5)  # Let the captured render frame catch up with accessibility semantics.
    (out/(name+'.png')).write_bytes(adb('exec-out','screencap','-p',binary=True))

assert package.endswith('.preview')
adb('shell','pm','clear',package)  # Only our disposable Preview app; never clears the published app.
adb('shell','am','start','-n',package+'/uz.meduza.anime.MainActivity')
wait('Nexa ID orqali kirish'); save('login')
root=snapshot(); text=' '.join((n.get('text') or n.get('content-desc') or '') for n in root.iter('node'))
for technical in ['auth.nexa.uz','PKCE','Email','Parol']:
    assert technical not in text, 'Technical/manual auth UI exposed: '+technical
print('PASS: single-provider sign-in UI, no manual credentials or technical auth copy', flush=True)
adb('shell','input','swipe','360','1300','360','500','350')
tap('Maxfiylik'); wait('Hujjatni yopish'); time.sleep(2); save('privacy-reader')
root=snapshot(); packages={n.get('package') for n in root.iter('node')}
assert 'com.android.chrome' not in packages
assert package in packages
print('PASS: legal document stays in-app with close control', flush=True)
tap('Hujjatni yopish'); tap('Mehmon sifatida ko‘rish'); wait('Profil'); save('guest-home')
tap('Profil'); wait('Siz mehmon sifatida kirdingiz'); save('guest-profile')
root=snapshot(); text=' '.join(n.get('text','') for n in root.iter('node'))
assert 'yuklanmadi' not in text.lower() and 'xatolik' not in text.lower()
print('PASS: guest profile shows guest copy and exit instead of profile API error', flush=True)
tap('Asosiy'); tap('Batafsil'); wait('Tomosha qilish uchun kiring'); save('guest-detail')
tap('Tomosha qilish uchun kiring'); wait('Hozir emas'); save('guest-playback-blocked')
print('PASS: guest playback is blocked by sign-in requirement', flush=True)
tap('Hozir emas'); adb('shell','input','swipe','360','1250','360','550','350'); tap('Qismlar · Kirish'); wait('Hozir emas'); tap('Hozir emas')
print('PASS: guest episode entry is blocked', flush=True)
adb('shell','input','keyevent','4'); tap('Profil'); tap('Chiqish'); wait('Nexa ID orqali kirish')
print('PASS: guest exit returns to sign-in', flush=True)
print('Screenshots:',out,flush=True)
