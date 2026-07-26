package com.openai.sunlitskins;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class AppearanceManager {
    public static final String TAG = "sunlitcompatible_appearance";
    private static final Map<String,List<Variant>> VARIANTS=loadVariants();
    private static final Set<String> RECOGNIZED=recognized();
    private static volatile boolean errorLogged;
    private AppearanceManager() {}

    public static void onAssigned(Object entity,Object pokemon) {
        if(entity==null||pokemon==null)return;
        try {
            if(isClient(entity)){restorePersistentAspect(pokemon);return;}
            String existing=readPersistent(pokemon);
            if(!existing.isBlank()){addAspects(pokemon,existing);return;}
            Set<String> current=aspects(pokemon);
            ArrayList<String> supplied=new ArrayList<>();
            for(String a:current)if(RECOGNIZED.contains(normalize(a)))supplied.add(normalize(a));
            if(!supplied.isEmpty()){String value=String.join(",",supplied);writePersistent(pokemon,value);addAspects(pokemon,value);return;}
            SunlitSkinsConfig cfg=SunlitSkinsConfig.get();
            if(!cfg.assignToWildPokemon)return;
            boolean wild=(Boolean)ReflectionUtil.method(pokemon.getClass(),"isWild",new String[]{"isWild"},0).invoke(pokemon);
            if(!wild)return;
            String species=speciesName(pokemon);
            List<Variant> options=VARIANTS.getOrDefault(species,List.of());
            if(options.isEmpty())return;
            ArrayList<Variant> enabled=new ArrayList<>();
            for(Variant v:options){
                if(v.shader&&!cfg.includeShaderEffects)continue;
                if(!v.shader&&!cfg.includeTextureVariants)continue;
                if(!cfg.familyEnabled(v.aspect))continue;
                enabled.add(v);
            }
            if(enabled.isEmpty()||ThreadLocalRandom.current().nextDouble(100.0)<cfg.normalChancePercent)return;
            Variant chosen=enabled.get(ThreadLocalRandom.current().nextInt(enabled.size()));
            writePersistent(pokemon,chosen.aspect); addAspects(pokemon,chosen.aspect);
        } catch(Throwable t){report("appearance assignment",t);}
    }

    public static void restorePersistentAspect(Object pokemon) {
        if(pokemon==null)return;
        try {String value=readPersistent(pokemon);if(!value.isBlank())addAspects(pokemon,value);} catch(Throwable t){report("appearance restoration",t);}
    }

    private static boolean isClient(Object entity)throws Exception {
        Object level=ReflectionUtil.method(entity.getClass(),"level",new String[]{"m_9236_","level"},0).invoke(entity);
        try{return ReflectionUtil.field(level.getClass(),"isClientSide","f_46443_","isClientSide").getBoolean(level);}catch(Throwable ignored){return false;}
    }
    private static String speciesName(Object pokemon)throws Exception {
        Object species=ReflectionUtil.method(pokemon.getClass(),"getSpecies",new String[]{"getSpecies"},0).invoke(pokemon);
        Object id=ReflectionUtil.method(species.getClass(),"getResourceIdentifier",new String[]{"getResourceIdentifier"},0).invoke(species);
        String s=String.valueOf(id).toLowerCase(Locale.ROOT);int colon=s.indexOf(':');return colon>=0?s.substring(colon+1):s;
    }
    @SuppressWarnings("unchecked") private static Set<String> aspects(Object pokemon)throws Exception {
        Object raw=ReflectionUtil.method(pokemon.getClass(),"getAspects",new String[]{"getAspects"},0).invoke(pokemon);
        return raw instanceof Set<?>?(Set<String>)raw:Set.of();
    }
    private static void addAspects(Object pokemon,String csv)throws Exception {
        LinkedHashSet<String> set=new LinkedHashSet<>(aspects(pokemon));
        for(String a:csv.split(","))if(!a.isBlank())set.add(normalize(a));
        ReflectionUtil.method(pokemon.getClass(),"setAspects",new String[]{"setAspects"},1).invoke(pokemon,set);
    }
    private static Object tag(Object pokemon)throws Exception{return ReflectionUtil.method(pokemon.getClass(),"getPersistentData",new String[]{"getPersistentData"},0).invoke(pokemon);}
    private static String readPersistent(Object pokemon)throws Exception {
        Object t=tag(pokemon);if(t==null)return "";
        for(String n:new String[]{"getString","m_128461_"})try{return String.valueOf(ReflectionUtil.method(t.getClass(),"tagGetString",new String[]{n},1).invoke(t,TAG));}catch(Throwable ignored){}
        return "";
    }
    private static void writePersistent(Object pokemon,String value)throws Exception {
        Object t=tag(pokemon);if(t==null)return;
        for(String n:new String[]{"putString","m_128359_"})try{ReflectionUtil.method(t.getClass(),"tagPutString",new String[]{n},2).invoke(t,TAG,value);return;}catch(Throwable ignored){}
        throw new NoSuchMethodException("CompoundTag.putString");
    }
    private static String normalize(String a){String s=a.toLowerCase(Locale.ROOT).trim();if(s.equals("radar_spawned"))return "mythical_holographic";if(s.equals("mythical_fireworks"))return "mythical_firework";return s;}
    private static Map<String,List<Variant>> loadVariants(){
        LinkedHashMap<String,List<Variant>>map=new LinkedHashMap<>();
        try(InputStream in=AppearanceManager.class.getClassLoader().getResourceAsStream("META-INF/sunlitcompatible/appearance-variants.tsv")){
            if(in==null)return Map.of();
            try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null){
                if(line.isBlank()||line.startsWith("#"))continue;String[]p=line.split("\\t",2);if(p.length<2)continue;
                ArrayList<Variant>list=new ArrayList<>();for(String token:p[1].split(",")){String[]v=token.split(":",2);if(v.length==2)list.add(new Variant(normalize(v[0]),v[1].equals("shader")));}
                map.put(p[0],Collections.unmodifiableList(list));
            }}
        }catch(Throwable t){report("variant table load",t);}
        return Collections.unmodifiableMap(map);
    }
    private static Set<String> recognized(){LinkedHashSet<String>s=new LinkedHashSet<>();for(List<Variant>l:VARIANTS.values())for(Variant v:l)s.add(v.aspect);return Collections.unmodifiableSet(s);}
    private static void report(String where,Throwable t){if(!errorLogged){errorLogged=true;System.err.println("[SunlitCompatibleSkins] Failed during "+where+": "+t);t.printStackTrace(System.err);}}
    private record Variant(String aspect,boolean shader){}
}
