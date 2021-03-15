package dev.lucasnlm.antimine.gdx.shaders

object BlurShader {
    const val resolution = "resolution"
    const val direction = "dir"
    const val radius = "radius"
    const val blurTop = "blurTop"
    const val blurBottom = "blurBottom"

    fun vert(): String {
        return """
            uniform mat4 u_projTrans;
        
            attribute vec4 a_position;
            attribute vec2 a_texCoord0;
            attribute vec4 a_color;
        
            varying vec4 v_color;
            varying vec2 v_texCoord;
        
            void main() {
                gl_Position = u_projTrans * a_position;
                v_texCoord = a_texCoord0;
                v_color = a_color;
            }
        """.trimIndent()
    }

    fun frag(): String {
        return """
            #ifdef GL_ES
            precision mediump float;
            precision mediump int;
            #else
            #define highp;
            #endif
            
            
            varying vec4 v_color;
            varying vec2 v_texCoord;
            
            uniform sampler2D u_texture;
            uniform float resolution;
            uniform float radius;
            uniform float blurTop;
            uniform float blurBottom;
            uniform vec2 dir;
            
            void main() {
                vec4 sum = vec4(0.0);
                vec2 tc = v_texCoord;
                
                if (tc.y > blurBottom && tc.y < blurTop && blurTop > 0.0) {
                    gl_FragColor = texture2D(u_texture, vec2(tc.x, tc.y));
                    return;
                }
                
                // Number of pixels off the central pixel to sample from
                float blur = radius/resolution; 
                
                // Blur direction
                float hstep = dir.x;
                float vstep = dir.y;
                
                // Apply blur using 9 samples and predefined gaussian weights
                sum += texture2D(u_texture, vec2(tc.x - 4.0*blur*hstep, tc.y - 4.0*blur*vstep)) * 0.006;
                sum += texture2D(u_texture, vec2(tc.x - 3.0*blur*hstep, tc.y - 3.0*blur*vstep)) * 0.044;
                sum += texture2D(u_texture, vec2(tc.x - 2.0*blur*hstep, tc.y - 2.0*blur*vstep)) * 0.121;
                sum += texture2D(u_texture, vec2(tc.x - 1.0*blur*hstep, tc.y - 1.0*blur*vstep)) * 0.194;
                
                sum += texture2D(u_texture, vec2(tc.x, tc.y)) * 0.27;
                
                sum += texture2D(u_texture, vec2(tc.x + 1.0*blur*hstep, tc.y + 1.0*blur*vstep)) * 0.194;
                sum += texture2D(u_texture, vec2(tc.x + 2.0*blur*hstep, tc.y + 2.0*blur*vstep)) * 0.121;
                sum += texture2D(u_texture, vec2(tc.x + 3.0*blur*hstep, tc.y + 3.0*blur*vstep)) * 0.044;
                sum += texture2D(u_texture, vec2(tc.x + 4.0*blur*hstep, tc.y + 4.0*blur*vstep)) * 0.006;
                
                gl_FragColor = sum;
            }
        """.trimIndent()
    }
}
