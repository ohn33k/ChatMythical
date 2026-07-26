package net.minecraft.client.renderer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
public abstract class RenderStateShard {
  public static ShaderStateShard f_173113_;
  public static TransparencyStateShard f_110134_;
  public static LightmapStateShard f_110152_;
  public static OverlayStateShard f_110154_;
  public RenderStateShard(String name, Runnable begin, Runnable end) {}
  public static class TexturingStateShard extends RenderStateShard { public TexturingStateShard(String n,Runnable b,Runnable e){super(n,b,e);} }
  public static class ShaderStateShard extends RenderStateShard { public ShaderStateShard(Supplier<?> s){super("",()->{},()->{});} }
  public static class EmptyTextureStateShard extends RenderStateShard { public EmptyTextureStateShard(){super("",()->{},()->{});} }
  public static class TextureStateShard extends EmptyTextureStateShard { public TextureStateShard(ResourceLocation r, boolean b1, boolean b2){} }
  public static class TransparencyStateShard extends RenderStateShard { public TransparencyStateShard(){super("",()->{},()->{});} }
  public static class LightmapStateShard extends RenderStateShard { public LightmapStateShard(){super("",()->{},()->{});} }
  public static class OverlayStateShard extends RenderStateShard { public OverlayStateShard(){super("",()->{},()->{});} }
}
