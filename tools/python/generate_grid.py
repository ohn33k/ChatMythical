#!/usr/bin/env python3
import argparse,json,math
from pathlib import Path

ORDER=['normal']

def pretty(a):
    if a=='normal':return 'Normal'
    return a.replace('mythical_','').replace('_',' ').title()

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--manifest',required=True);ap.add_argument('--output',required=True);ap.add_argument('--columns',type=int,default=8);ap.add_argument('--rows',type=int,default=8);a=ap.parse_args()
    d=json.load(open(a.manifest,encoding='utf-8'))
    entries=[];known=set()
    for s in d['entries']:
        apps=['normal']+s['static_variants']+s['animated_variants']
        for app in apps:
            entries.append([s['dex'],s['species'],s['display_name'],app,pretty(app)])
            if app!='normal':known.add(app)
    per=a.columns*a.rows;pages=math.ceil(len(entries)/per)
    js_entries=json.dumps(entries,separators=(',',':'),ensure_ascii=False)
    text=f'''// Sunlit Compatible Skins Grid v0.2
// Cobblemon Forge 1.20.1 / Cobblemon 1.5.2
// Sequence: lowest National Dex number to highest. For each species: Normal,
// then every accepted static skin, then the seven animated shader appearances.
// Commands: /sunlitskingrid spawn <page>, next, previous, respawn, clear, info

(function sunlitSkinGridV02() {{
console.info('[SUNLIT-SKIN-GRID] sunlitSkinGrid.js v0.2 loaded');
const $PokemonProperties = Java.loadClass('com.cobblemon.mod.common.api.pokemon.PokemonProperties');
const GRID_TAG = 'sunlit_skin_grid';
const APPEARANCE_TAG = 'sunlitcompatible_appearance';
const COLUMNS = {a.columns};
const ROWS = {a.rows};
const CELLS_PER_PAGE = COLUMNS * ROWS;
const CELL_X = 12;
const CELL_Z = 12;
const SPAWNS_PER_TICK = 3;
const ENTRIES = {js_entries};
const PAGE_COUNT = Math.ceil(ENTRIES.length / CELLS_PER_PAGE);

ServerEvents.commandRegistry(event => {{
  const {{ commands: Commands, arguments: Arguments }} = event;
  event.register(Commands.literal('sunlitskingrid').requires(s => s.hasPermission(2))
    .then(Commands.literal('spawn').then(Commands.argument('page', Arguments.INTEGER.create(event)).executes(c => spawnPage(c.source, Arguments.INTEGER.getResult(c,'page')))))
    .then(Commands.literal('next').executes(c => changePage(c.source,1)))
    .then(Commands.literal('previous').executes(c => changePage(c.source,-1)))
    .then(Commands.literal('respawn').executes(c => spawnPage(c.source,currentPage(c.source.server))))
    .then(Commands.literal('clear').executes(c => clearGrid(c.source)))
    .then(Commands.literal('info').executes(c => showInfo(c.source))));
}});

function player(source) {{ return source.player || null; }}
function sunlitGridState(server) {{
  if (!server.persistentData.sunlitSkinGridPage) server.persistentData.sunlitSkinGridPage=1;
  if (!server.persistentData.sunlitSkinGridGeneration) server.persistentData.sunlitSkinGridGeneration=0;
  return server.persistentData;
}}
function currentPage(server) {{ return clamp(sunlitGridState(server).sunlitSkinGridPage); }}
function clamp(v) {{ v=Math.floor(Number(v)||1); return Math.max(1,Math.min(PAGE_COUNT,v)); }}
function clearEntities(source) {{
  const p=player(source); if(!p)return false;
  source.server.runCommandSilent(`execute in ${{p.level.dimension}} run kill @e[tag=${{GRID_TAG}}]`);
  sunlitGridState(source.server).sunlitSkinGridGeneration=Number(sunlitGridState(source.server).sunlitSkinGridGeneration)+1;
  return true;
}}
function clearGrid(source) {{ const p=player(source);if(!p)return -1;clearEntities(source);p.tell(Text.of('[Sunlit Skin Grid] Cleared.').green());return 1; }}
function changePage(source,dir) {{ let p=currentPage(source.server)+dir;if(p<1)p=PAGE_COUNT;if(p>PAGE_COUNT)p=1;return spawnPage(source,p); }}
function showInfo(source) {{
  const p=player(source);if(!p)return -1;const page=currentPage(source.server);const start=(page-1)*CELLS_PER_PAGE;const end=Math.min(ENTRIES.length,start+CELLS_PER_PAGE);
  p.tell(Text.of(`[Sunlit Skin Grid] Page ${{page}}/${{PAGE_COUNT}} · entries ${{start+1}}-${{end}} of ${{ENTRIES.length}}`).gold());
  p.tell(Text.of('/sunlitskingrid next | previous | respawn | clear').gray());return 1;
}}
function spawnPage(source,requested) {{
  const p=player(source);if(!p)return -1;const server=source.server;const level=p.level;const page=clamp(requested);const d=sunlitGridState(server);
  clearEntities(source);d.sunlitSkinGridPage=page;const generation=Number(d.sunlitSkinGridGeneration);
  const start=(page-1)*CELLS_PER_PAGE;const cells=ENTRIES.slice(start,start+CELLS_PER_PAGE);
  const originX=Math.floor(p.x)+10;const originY=Math.floor(p.y)+1;const originZ=Math.floor(p.z)+14;
  spawnLabel(server,p.level.dimension,originX-6,originY+3.8,originZ-6,`Page ${{page}}/${{PAGE_COUNT}} · ${{start+1}}-${{start+cells.length}}`,'yellow');
  cells.forEach((entry,index)=>{{
    const col=index%COLUMNS,row=Math.floor(index/COLUMNS);const x=originX+col*CELL_X,z=originZ+row*CELL_Z;
    const dex=String(entry[0]).padStart(4,'0');spawnLabel(server,p.level.dimension,x,originY+3.5,z,`#${{dex}} ${{entry[2]}}\\n${{entry[4]}}`,entry[3]==='normal'?'gold':'aqua');
    const delay=Math.floor(index/SPAWNS_PER_TICK);
    server.scheduleInTicks(delay,()=>{{
      if(Number(sunlitGridState(server).sunlitSkinGridGeneration)!==generation)return;
      try {{ spawnPokemon(level,entry[1],entry[3],x,originY,z); }}
      catch(error) {{ console.error(`[SUNLIT-SKIN-GRID] Failed #${{dex}} ${{entry[1]}} / ${{entry[3]}}: ${{error}}`);spawnLabel(server,p.level.dimension,x,originY+2,z,'SPAWN ERROR','red'); }}
    }});
  }});
  p.tell(Text.of(`[Sunlit Skin Grid] Spawning page ${{page}}/${{PAGE_COUNT}} with ${{cells.length}} appearances in National Dex order.`).green());
  return 1;
}}
function spawnPokemon(level,species,appearance,x,y,z) {{
  const propertyText=appearance==='normal'?`${{species}} level=50`:`${{species}} level=50 ${{appearance}}`;
  const properties=$PokemonProperties.Companion.parse(propertyText);
  const entity=properties.createEntity(level);const pokemon=entity.getPokemon();
  if(appearance!=='normal' && !pokemon.getAspects().contains(appearance))throw new Error(`Property parser rejected ${{appearance}}`);
  pokemon.getPersistentData().putString(APPEARANCE_TAG,appearance==='normal'?'':appearance);
  try{{entity.setPokemon(pokemon);}}catch(ignored){{}}
  try{{entity.moveTo(x+0.5,y,z+0.5,180.0,0.0);}}catch(ignored){{entity.setPos(x+0.5,y,z+0.5);}}
  entity.addTag(GRID_TAG);try{{entity.setInvulnerable(true);}}catch(ignored){{}}try{{entity.setSilent(true);}}catch(ignored){{}}try{{entity.setPersistenceRequired();}}catch(ignored){{}}
  const added=level.addFreshEntity(entity);if(added===false)throw new Error('ServerLevel rejected entity');
  try{{entity.setNoAi(true);}}catch(ignored){{}}
}}
function spawnLabel(server,dimension,x,y,z,text,color) {{
  const component=JSON.stringify({{text:String(text),color:color}}).replace(/'/g,"\\\\'");
  server.runCommandSilent(`execute in ${{dimension}} run summon minecraft:armor_stand ${{x}} ${{y}} ${{z}} {{Invisible:1b,Marker:1b,NoGravity:1b,Invulnerable:1b,Silent:1b,CustomNameVisible:1b,CustomName:'${{component}}',Tags:["${{GRID_TAG}}"]}}`);
}}
}})();
'''
    out=Path(a.output);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(text,encoding='utf-8')
    print(json.dumps({'entries':len(entries),'pages':pages,'columns':a.columns,'rows':a.rows,'output':str(out)},indent=2))
if __name__=='__main__':main()
