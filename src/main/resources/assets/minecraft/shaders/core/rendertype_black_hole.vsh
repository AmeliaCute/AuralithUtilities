#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;
in ivec2 UV1;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 vPosOS;
out vec3 vNormalOS;

void main()
{
    vec3 posOS = Position;
    vPosOS = posOS;
    vNormalOS = normalize(Normal);

    vec3 worldPos = posOS + ChunkOffset;
    vec4 viewPos = ModelViewMat * vec4(worldPos, 1.0);
    gl_Position = ProjMat * viewPos;

    vertexDistance = length(viewPos.xyz);
    vertexColor = Color;
    texCoord0 = UV0;
}
