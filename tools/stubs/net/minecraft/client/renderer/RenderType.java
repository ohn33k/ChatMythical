package net.minecraft.client.renderer;
import com.mojang.blaze3d.vertex.VertexFormat;
public abstract class RenderType extends RenderStateShard {
  public RenderType(String n, VertexFormat f, VertexFormat.Mode m, int s, boolean c, boolean t, Runnable b, Runnable e){super(n,b,e);}
  public static CompositeRenderType m_173215_(String n, VertexFormat f, VertexFormat.Mode m, int size, boolean crumble, boolean translucent, CompositeState state){return null;}
  public static class CompositeRenderType extends RenderType {
    public CompositeRenderType(){super("",null,null,0,false,false,()->{},()->{});}
  }
  public static final class CompositeState {
    public static CompositeStateBuilder m_110628_(){return null;}
    public static final class CompositeStateBuilder {
      public CompositeStateBuilder m_173292_(RenderStateShard.ShaderStateShard x){return this;}
      public CompositeStateBuilder m_173290_(RenderStateShard.EmptyTextureStateShard x){return this;}
      public CompositeStateBuilder m_110685_(RenderStateShard.TransparencyStateShard x){return this;}
      public CompositeStateBuilder m_110671_(RenderStateShard.LightmapStateShard x){return this;}
      public CompositeStateBuilder m_110677_(RenderStateShard.OverlayStateShard x){return this;}
      public CompositeStateBuilder m_110683_(RenderStateShard.TexturingStateShard x){return this;}
      public CompositeState m_110691_(boolean outline){return null;}
    }
  }
}
