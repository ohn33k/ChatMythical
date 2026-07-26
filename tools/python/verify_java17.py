#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, struct, zipfile
from pathlib import Path

def main() -> None:
    ap=argparse.ArgumentParser()
    ap.add_argument('jar',type=Path)
    args=ap.parse_args()
    above=[]; maximum=0; count=0
    with zipfile.ZipFile(args.jar) as z:
        bad=z.testzip()
        if bad: raise SystemExit(f'Corrupt ZIP entry: {bad}')
        for name in z.namelist():
            if not name.endswith('.class'): continue
            data=z.read(name)
            if len(data)<8 or data[:4]!=b'\xca\xfe\xba\xbe': continue
            major=struct.unpack('>H',data[6:8])[0]
            maximum=max(maximum,major); count+=1
            if major>61: above.append({'class':name,'major':major})
    print(json.dumps({'class_count':count,'maximum_class_major':maximum,'java17_maximum':61,'above_java17':above},indent=2))
    if above: raise SystemExit(1)

if __name__=='__main__': main()
