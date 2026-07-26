#!/usr/bin/env python3
"""Build a strict texture-only overlay against Cobblemon 1.5.2's native visual stack."""
from __future__ import annotations
import argparse,csv,io,json,zipfile,collections,shutil
from pathlib import Path, PurePosixPath
from PIL import Image

SHADERS=['mythical_radiant','mythical_magma','mythical_glitch','mythical_galaxy','mythical_matrix','mythical_firework','mythical_holographic']
ALIASES={'radar_spawned':'mythical_holographic','mythical_fireworks':'mythical_firework'}

def norm(a):
    a=str(a).lower().strip(); return ALIASES.get(a,a)
def readj(z,n): return json.loads(z.read(n))
def writej(path,obj):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
def refpath(ref):
    ns,p=(ref.split(':',1) if ':' in ref else ('cobblemon',ref)); return f'assets/{ns}/{p}'
def alpha(z,n):
    im=Image.open(io.BytesIO(z.read(n))).convert('RGBA')
    return im.size, tuple(v>8 for v in im.getchannel('A').getdata())
def iou(a,b):
    if a is None or b is None or len(a)!=len(b): return -1.0
    inter=sum(x and y for x,y in zip(a,b)); union=sum(x or y for x,y in zip(a,b)); return inter/union if union else 1.0

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--cobblemon',required=True)
    ap.add_argument('--mythical',required=True)
    ap.add_argument('--output',required=True)
    ap.add_argument('--audit',required=True)
    ap.add_argument('--manifest',required=True)
    ap.add_argument('--threshold',type=float,default=0.97)
    args=ap.parse_args()
    out=Path(args.output); out.mkdir(parents=True,exist_ok=True)
    # Remove generated v0.x resources before rebuilding so a previously accepted
    # family cannot survive as a stale assignment, resolver, texture, or manifest.
    for rel in [
        'data/cobblemon/species_features/sunlitcompatible',
        'data/cobblemon/species_feature_assignments/sunlitcompatible',
        'assets/cobblemon/bedrock/pokemon/resolvers/sunlitcompatible',
        'assets/mythicalcobbled',
        'META-INF/sunlitcompatible'
    ]:
        shutil.rmtree(out/rel,ignore_errors=True)
    audit=[]; accepted_by_species=collections.defaultdict(list); family_species=collections.defaultdict(set); base_family_species=collections.defaultdict(set)
    copied=set(); source_seen=set()

    with zipfile.ZipFile(args.cobblemon) as base, zipfile.ZipFile(args.mythical) as src:
        base_names=set(base.namelist()); src_names=set(src.namelist())
        species={}
        for n in base.namelist():
            if n.startswith('data/cobblemon/species/') and n.endswith('.json'):
                try:d=readj(base,n)
                except Exception:continue
                if d.get('implemented'):
                    sp=PurePosixPath(n).stem
                    species[sp]={'dex':int(d.get('nationalPokedexNumber',99999)),'name':d.get('name',sp)}

        basevars=collections.defaultdict(list)
        species_has_model=set()
        for n in base.namelist():
            if not(n.startswith('assets/cobblemon/bedrock/pokemon/resolvers/') and n.endswith('.json')):continue
            try:d=readj(base,n)
            except Exception:continue
            sp=str(d.get('species','')).split(':')[-1]
            if sp not in species:continue
            for v in d.get('variations',[]):
                if not isinstance(v,dict):continue
                aspects=frozenset(norm(a) for a in v.get('aspects',[]))
                if isinstance(v.get('texture'),str):basevars[sp].append((aspects,v,n))
                if isinstance(v.get('model'),str):species_has_model.add(sp)
        supported={sp for sp in species if sp in species_has_model and basevars.get(sp)}

        for n in src.namelist():
            if not(n.startswith('assets/cobblemon/bedrock/pokemon/resolvers/') and n.endswith('.json')):continue
            try:d=readj(src,n)
            except Exception as e:
                audit.append({'species':'','dex':'','family':'','base_aspects':'','source_resolver':n,'texture':'','base_texture':'','alpha_iou':'','status':'rejected','reason':'invalid_resolver_json'});continue
            sp=str(d.get('species','')).split(':')[-1]
            for v in d.get('variations',[]):
                if not isinstance(v,dict):continue
                aspects=[norm(a) for a in v.get('aspects',[])]
                families=sorted(set(a for a in aspects if a.startswith('mythical_')))
                if not families:continue
                source_seen.add(sp)
                row={'species':sp,'dex':species.get(sp,{}).get('dex',''),'family':','.join(families),'base_aspects':','.join(a for a in aspects if not a.startswith('mythical_')),'source_resolver':n,'texture':v.get('texture',''),'base_texture':'','alpha_iou':'','status':'rejected','reason':''}
                if len(families)!=1: row['reason']='multiple_cosmetic_families';audit.append(row);continue
                fam=families[0]
                if sp not in supported:row['reason']='species_not_in_cobblemon_1_5_2_visual_stack';audit.append(row);continue
                if any(k in v for k in ('model','poser')):row['reason']='custom_model_or_poser';audit.append(row);continue
                tex=v.get('texture')
                if not isinstance(tex,str):
                    # Effect-only entries are represented globally, not copied from source resolvers.
                    row['reason']='effect_only_or_no_texture';audit.append(row);continue
                base_aspects=frozenset(a for a in aspects if not a.startswith('mythical_'))
                candidates=[x for x in basevars[sp] if x[0].issubset(base_aspects)]
                if not candidates:row['reason']='no_matching_base_texture';audit.append(row);continue
                candidates.sort(key=lambda x:(len(x[0]),x[0]==base_aspects),reverse=True)
                _,bv,_=candidates[0]; btex=bv['texture']; row['base_texture']=btex
                spath,bpath=refpath(tex),refpath(btex)
                if spath not in src_names:row['reason']='source_texture_missing';audit.append(row);continue
                if bpath not in base_names:row['reason']='base_texture_missing';audit.append(row);continue
                try:ss,sm=alpha(src,spath);bs,bm=alpha(base,bpath)
                except Exception:row['reason']='texture_read_error';audit.append(row);continue
                score=iou(sm,bm) if ss==bs else -1.0;row['alpha_iou']=f'{score:.6f}'
                if ss!=bs:row['reason']=f'dimension_mismatch_{ss[0]}x{ss[1]}_vs_{bs[0]}x{bs[1]}';audit.append(row);continue
                if score<args.threshold:row['reason']=f'alpha_layout_below_{args.threshold:.2f}';audit.append(row);continue

                clean={'aspects':aspects,'texture':tex}
                if isinstance(v.get('layers'),list):
                    layers=[]
                    for layer in v['layers']:
                        if not isinstance(layer,dict):continue
                        layer=dict(layer);layer.pop('effectname',None);layer.pop('ghost',None)
                        if 'texture_1.6' in layer and 'texture' not in layer:layer['texture']=layer.pop('texture_1.6')
                        ltex=layer.get('texture')
                        if isinstance(ltex,str) and refpath(ltex) in src_names:
                            layers.append(layer)
                    if layers:clean['layers']=layers
                accepted_by_species[sp].append(clean);family_species[fam].add(sp)
                if not base_aspects:
                    base_family_species[fam].add(sp)
                row['status']='accepted';row['reason']='strict_native_uv_match';audit.append(row)
                refs=[tex]+[x['texture'] for x in clean.get('layers',[]) if isinstance(x.get('texture'),str)]
                for ref in refs:
                    p=refpath(ref)
                    if p in copied:continue
                    target=out/p;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(src.read(p));copied.add(p)

        # Write one resolver per species, preserving only strict texture/layer variations.
        for sp,vars_ in accepted_by_species.items():
            dex=species[sp]['dex']
            rel=f'assets/cobblemon/bedrock/pokemon/resolvers/sunlitcompatible/{dex:04d}_{sp}/zz_sunlit_compatible_skins.json'
            writej(out/rel,{'species':f'cobblemon:{sp}','order':90,'variations':vars_})

        # Register every included appearance as an actual Cobblemon flag feature.
        # The earlier alpha accidentally emitted assignments without definitions, which
        # made /pokespawn reject the mythical_* properties and removed autocomplete.
        all_supported=sorted(supported,key=lambda s:(species[s]['dex'],s))
        included_families=sorted(set(base_family_species)|set(SHADERS))
        for fam in included_families:
            writej(out/f'data/cobblemon/species_features/sunlitcompatible/{fam}.json',{
                'type':'flag','keys':[fam],'default':False,'isAspect':True
            })
            # Static flags are assigned only where a default-form texture passed.
            # Animated shader flags are valid for every species in the native stack.
            mons=all_supported if fam in SHADERS else sorted(base_family_species[fam],key=lambda s:(species[s]['dex'],s))
            writej(out/f'data/cobblemon/species_feature_assignments/sunlitcompatible/{fam}.json',{'pokemon':mons,'features':[fam]})

        # Runtime variant table and machine-readable manifest.
        resources=out/'META-INF/sunlitcompatible';resources.mkdir(parents=True,exist_ok=True)
        manifest=[];tsv=['# species\taspect:kind,...']
        for sp in sorted(supported,key=lambda s:(species[s]['dex'],s)):
            static=[]
            # Grid and random selection expose a family only when its base-form, non-shiny texture passed.
            for fam in sorted(base_family_species):
                if sp in base_family_species[fam]:static.append(fam)
            tokens=[f'{x}:texture' for x in static]+[f'{x}:shader' for x in SHADERS]
            tsv.append(sp+'\t'+','.join(tokens))
            manifest.append({'dex':species[sp]['dex'],'species':sp,'display_name':species[sp]['name'],'static_variants':static,'animated_variants':SHADERS})
        (resources/'appearance-variants.tsv').write_text('\n'.join(tsv)+'\n',encoding='utf-8')
        manifest_obj={'version':2,'base':'Cobblemon 1.5.2','strict_alpha_iou_threshold':args.threshold,'supported_species':len(manifest),'accepted_texture_variations':sum(1 for r in audit if r['status']=='accepted'),'entries':manifest}
        writej(Path(args.manifest),manifest_obj);writej(resources/'compatibility-manifest.json',manifest_obj)
        (resources/'README.txt').write_text('This overlay contains no replacement Pokémon models, posers, or animations. Static skins passed a strict UV alpha-layout comparison against the native Cobblemon 1.5.2 texture. Animated families use the existing Oculus/shader render bridge.\n',encoding='utf-8')

    fields=['species','dex','family','base_aspects','source_resolver','texture','base_texture','alpha_iou','status','reason']
    Path(args.audit).parent.mkdir(parents=True,exist_ok=True)
    with open(args.audit,'w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(audit)
    print(json.dumps({'supported_species':len(supported),'accepted_rows':sum(r['status']=='accepted' for r in audit),'rejected_rows':sum(r['status']=='rejected' for r in audit),'copied_textures':len(copied),'overlay':str(out)},indent=2))
if __name__=='__main__':main()
