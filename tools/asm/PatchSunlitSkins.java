import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import jdk.internal.org.objectweb.asm.*;

public final class PatchSunlitSkins {
  static final String POKEMON="com/cobblemon/mod/common/pokemon/Pokemon.class";
  static final String ENTITY="com/cobblemon/mod/common/entity/pokemon/PokemonEntity.class";
  static final String PROPERTIES_ARG="com/cobblemon/mod/common/command/argument/PokemonPropertiesArgumentType.class";

  public static void main(String[] a)throws Exception{
    if(a.length!=4)throw new IllegalArgumentException("in out helperDir marker");
    Path in=Path.of(a[0]),out=Path.of(a[1]),helpers=Path.of(a[2]);
    Map<String,byte[]> r=new LinkedHashMap<>();
    r.put(POKEMON,patchPokemon(read(in,POKEMON)));
    r.put(ENTITY,patchEntity(read(in,ENTITY)));
    r.put(PROPERTIES_ARG,patchPropertiesAutocomplete(read(in,PROPERTIES_ARG)));
    Files.walk(helpers).filter(Files::isRegularFile).filter(p->p.toString().endsWith(".class")).forEach(p->{
      try{r.put(helpers.relativize(p).toString().replace(File.separatorChar,'/'),Files.readAllBytes(p));}
      catch(IOException e){throw new UncheckedIOException(e);}
    });
    r.put("SUNLIT_COMPATIBLE_SKINS_BUILD.txt",("Sunlit Compatible Skins\n"+a[3]+"\nUses only Cobblemon 1.5.2 base model/poser/animation assets.\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
    rewrite(in,out,r);
  }

  static byte[] patchPokemon(byte[] b){
    ClassReader cr=new ClassReader(b);ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);int[]n={0};
    ClassVisitor cv=new ClassVisitor(Opcodes.ASM8,cw){public MethodVisitor visitMethod(int ac,String name,String desc,String sig,String[]ex){
      MethodVisitor mv=super.visitMethod(ac,name,desc,sig,ex);
      if(name.equals("updateAspects")&&desc.equals("()V")){n[0]++;return new MethodVisitor(Opcodes.ASM8,mv){public void visitInsn(int op){
        if(op==Opcodes.RETURN){super.visitVarInsn(Opcodes.ALOAD,0);super.visitMethodInsn(Opcodes.INVOKESTATIC,"com/openai/sunlitskins/AppearanceManager","restorePersistentAspect","(Ljava/lang/Object;)V",false);}super.visitInsn(op);
      }};}return mv;
    }};cr.accept(cv,0);if(n[0]!=1)throw new IllegalStateException("updateAspects="+n[0]);return cw.toByteArray();
  }

  static byte[] patchEntity(byte[] b){
    ClassReader cr=new ClassReader(b);ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);int[]ct={0},st={0};
    ClassVisitor cv=new ClassVisitor(Opcodes.ASM8,cw){public MethodVisitor visitMethod(int ac,String name,String desc,String sig,String[]ex){
      MethodVisitor mv=super.visitMethod(ac,name,desc,sig,ex);
      boolean ctor=name.equals("<init>")&&desc.startsWith("(Lnet/minecraft/world/level/Level;Lcom/cobblemon/mod/common/pokemon/Pokemon;Lnet/minecraft/world/entity/EntityType;");
      boolean setter=name.equals("setPokemon")&&desc.equals("(Lcom/cobblemon/mod/common/pokemon/Pokemon;)V");
      if(!ctor&&!setter)return mv;
      return new MethodVisitor(Opcodes.ASM8,mv){public void visitFieldInsn(int op,String owner,String field,String fd){
        super.visitFieldInsn(op,owner,field,fd);
        if(op==Opcodes.PUTFIELD&&owner.equals("com/cobblemon/mod/common/entity/pokemon/PokemonEntity")&&field.equals("pokemon")){
          super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,ctor?2:1);
          super.visitMethodInsn(Opcodes.INVOKESTATIC,"com/openai/sunlitskins/AppearanceManager","onAssigned","(Ljava/lang/Object;Ljava/lang/Object;)V",false);
          if(ctor)ct[0]++;else st[0]++;
        }
      }};
    }};cr.accept(cv,0);if(ct[0]!=1||st[0]!=1)throw new IllegalStateException("entity patches ctor="+ct[0]+" set="+st[0]);return cw.toByteArray();
  }

  static byte[] patchPropertiesAutocomplete(byte[] b){
    ClassReader cr=new ClassReader(b);ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);int[]suggest={0},initial={0};
    ClassVisitor cv=new ClassVisitor(Opcodes.ASM8,cw){public MethodVisitor visitMethod(int ac,String name,String desc,String sig,String[]ex){
      MethodVisitor mv=super.visitMethod(ac,name,desc,sig,ex);
      return new MethodVisitor(Opcodes.ASM8,mv){public void visitMethodInsn(int op,String owner,String methodName,String methodDesc,boolean itf){
        if(op==Opcodes.INVOKEVIRTUAL
          && owner.equals("com/cobblemon/mod/common/pokemon/properties/PropertiesCompletionProvider")
          && methodName.equals("suggestKeys")
          && methodDesc.equals("(Ljava/lang/String;Ljava/util/Collection;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;")){
          suggest[0]++;
          super.visitMethodInsn(Opcodes.INVOKESTATIC,
            "com/openai/sunlitskins/SpeciesFilteredSuggestions","suggestKeys",
            "(Lcom/cobblemon/mod/common/pokemon/properties/PropertiesCompletionProvider;Ljava/lang/String;Ljava/util/Collection;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",false);
          return;
        }
        if(op==Opcodes.INVOKEVIRTUAL
          && owner.equals("com/cobblemon/mod/common/pokemon/properties/PropertiesCompletionProvider")
          && methodName.equals("keys")
          && methodDesc.equals("()Ljava/util/List;")){
          initial[0]++;
          super.visitMethodInsn(Opcodes.INVOKESTATIC,
            "com/openai/sunlitskins/SpeciesFilteredSuggestions","initialKeys",
            "(Lcom/cobblemon/mod/common/pokemon/properties/PropertiesCompletionProvider;)Ljava/util/List;",false);
          return;
        }
        super.visitMethodInsn(op,owner,methodName,methodDesc,itf);
      }};
    }};cr.accept(cv,0);
    if(suggest[0]!=1||initial[0]!=1)throw new IllegalStateException("autocomplete patches suggest="+suggest[0]+" initial="+initial[0]);
    return cw.toByteArray();
  }

  static byte[] read(Path p,String e)throws IOException{try(JarFile j=new JarFile(p.toFile())){JarEntry x=j.getJarEntry(e);if(x==null)throw new FileNotFoundException(e);try(InputStream in=j.getInputStream(x)){return in.readAllBytes();}}}
  static void rewrite(Path in,Path out,Map<String,byte[]>r)throws IOException{
    Files.deleteIfExists(out);Set<String>w=new HashSet<>();
    try(JarFile j=new JarFile(in.toFile());JarOutputStream z=new JarOutputStream(Files.newOutputStream(out))){
      Enumeration<JarEntry>es=j.entries();while(es.hasMoreElements()){JarEntry e=es.nextElement();String name=e.getName(),u=name.toUpperCase(Locale.ROOT);
        if(u.startsWith("META-INF/")&&(u.endsWith(".SF")||u.endsWith(".RSA")||u.endsWith(".DSA")))continue;
        JarEntry ne=new JarEntry(name);ne.setTime(0);z.putNextEntry(ne);if(!e.isDirectory()){byte[]x=r.get(name);if(x!=null)z.write(x);else try(InputStream q=j.getInputStream(e)){q.transferTo(z);}}z.closeEntry();w.add(name);
      }
      for(var x:r.entrySet())if(!w.contains(x.getKey())){JarEntry ne=new JarEntry(x.getKey());ne.setTime(0);z.putNextEntry(ne);z.write(x.getValue());z.closeEntry();}
    }
  }
}
