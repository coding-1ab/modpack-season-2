layout (vertices = 4) out;
in vec2 texCoord0[];
out vec2 texCoord1[];
in vec4 vertexColor0[];
out vec4 vertexColor1[];
in float vertexDistance0[];
out float vertexDistance1[];

void main(void)
{
    if (gl_InvocationID == 0) // to not do same stuff 4 times
    {
        gl_TessLevelInner[0] = 4;
        gl_TessLevelInner[1] = 4;

        gl_TessLevelOuter[0] = 4;
        gl_TessLevelOuter[1] = 4;
        gl_TessLevelOuter[2] = 4;
        gl_TessLevelOuter[3] = 4;
    }

    texCoord1[gl_InvocationID] = texCoord0[gl_InvocationID];
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    vertexColor1[gl_InvocationID] = vertexColor0[gl_InvocationID];
    vertexDistance1[gl_InvocationID] = vertexDistance0[gl_InvocationID];

}