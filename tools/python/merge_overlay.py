#!/usr/bin/env python3
import argparse,zipfile,os
from pathlib import Path
ap=argparse.ArgumentParser();ap.add_argument('--base',required=True);ap.add_argument('--overlay',required=True);ap.add_argument('--output',required=True);a=ap.parse_args()
over=Path(a.overlay);repl={p.relative_to(over).as_posix():p for p in over.rglob('*') if p.is_file()}
with zipfile.ZipFile(a.base) as src, zipfile.ZipFile(a.output,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as dst:
    written=set()
    for info in src.infolist():
        name=info.filename;upper=name.upper()
        if upper.startswith('META-INF/') and upper.endswith(('.SF','.RSA','.DSA')):continue
        if name in repl:
            dst.writestr(name,repl[name].read_bytes());written.add(name)
        else:dst.writestr(info,src.read(name))
    for name,p in repl.items():
        if name not in written:dst.writestr(name,p.read_bytes())
print(f'Merged {len(repl)} overlay files into {a.output}')
