#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in float vertexDistance;
in vec4  vertexColor;
in vec2  texCoord0;
in vec3  vPosOS;
in vec3  vNormalOS;

out vec4 fragColor;

const float PI = 3.14159265358979323846;

float hash(vec2 p)
{
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p)
{
    float value = 0.0;
    float amplitude = 0.5;

    for (int i = 0; i < 5; ++i)
    {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

void main()
{
    vec2 flipped = vec2(1.225 - texCoord0.x,0.75 - (texCoord0.y / 2));
    vec2 centered = (flipped - 0.5) * 2.0;

    float dist = length(centered);

    float eventHorizon = 0.25;
    float diskInner = 0.20;
    float diskOuter = 1.00;

    float time = GameTime * 20.0;

    vec3 color = vec3(0.0);
    float alpha = 1.0;

    // Inside event horizon -> black
    if (dist < eventHorizon)
    {
        color = vec3(0.0);
        alpha = 1.0;
    }
    else if (dist >= diskInner && dist <= diskOuter)
    {
        float diskMask = smoothstep(diskInner, diskInner + 0.05, dist)
        * (1.0 - smoothstep(diskOuter - 0.05, diskOuter, dist));

        float angle = atan(centered.x, centered.y);
        vec2 polarCoord = vec2(dist * 10.0, angle * 3.0 + time * 0.5);

        float turbulence = fbm(polarCoord + time * 0.1);

        float temp = clamp(1.0 - (dist - diskInner) / max(0.0001, (diskOuter - diskInner)), 0.0, 1.0);

        vec3 hotColor = mix(vec3(1.0, 0.9, 0.4), vec3(1.0, 0.3, 0.0), pow(1.0 - temp, 1.5));
        hotColor *= (0.7 + 0.3 * turbulence);

        float emission = (1.5 + 0.3 * sin(time * 0.3)) * temp;

        color = hotColor * emission * diskMask;
        alpha = clamp(diskMask, 0.0, 1.0);
    }
    else if (dist < diskInner)
    {
        float lensZone = clamp((dist - eventHorizon) / max(0.0001, (diskInner - eventHorizon)), 0.0, 1.0);

        vec2 distortedUV = centered * (1.0 + 0.3 * (1.0 - lensZone));
        float distortNoise = fbm(distortedUV * 5.0 + time * 0.05);

        vec3 lensGlow = vec3(0.8, 0.4, 0.1) * distortNoise * 0.6;

        color = lensGlow;
        alpha = 0.35 + 0.5 * distortNoise * (1.0 - lensZone);
    }

    {
        float thickness = 0.06;
        float edge = abs(dist - eventHorizon);
        float hawkingGlow = smoothstep(thickness, 0.0, edge);
        vec3 hawkingColor = vec3(0.4, 0.6, 1.0) * 0.8;
        color += hawkingColor * hawkingGlow;
    }

    float flicker = 0.95 + 0.05 * sin(time * 5.0 + dist * 10.0);
    color *= flicker;

    vec4 finalColor = vec4(color * 2.0, alpha) * vertexColor * ColorModulator;

    float fogValue = smoothstep(FogStart, FogEnd, vertexDistance);
    finalColor.rgb = mix(finalColor.rgb, FogColor.rgb, fogValue * FogColor.a);

    if (finalColor.a <= 0.001) discard;

    fragColor = finalColor;
}