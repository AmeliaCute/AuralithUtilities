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
in vec4  normal;

out vec4 fragColor;

float hash(vec2 p)
{
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43764.3493);
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

    return mix(mix(a,b, f.x), mix(c,d,f.x), f.y);
}

float fbm(vec2 p)
{
    float value = 0.0;
    float amplitude = 0.5;

    for(int i = 0; i < 5; ++i)
    {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

void main()
{
    vec2 uv = vec2(1.0 -(texCoord0.x - 0.25) * 2.0, 1.0 - (texCoord0.y) * 2);
    float dist = length(uv);

    float eventHorizon = 0.50;

    float diskInner = 0.3;
    float diskOuter = 0.85;

    float time = GameTime * 20.0;

    vec3 color = vec3(0.0);
    float alpha = 1.0;

    if (dist < eventHorizon)
    {
        color = vec3(0.0);
        alpha = 1.0;
    }

    else if(dist >= diskInner && dist <= diskOuter)
    {
        float diskMask = smoothstep(diskInner, diskInner + 0.05, dist) *
        smoothstep(diskOuter, diskOuter - 0.1, dist);

        float angle = atan(uv.y, uv.x);
        vec2 polarCoord = vec2(dist * 10.0, angle * 3.0 + time * 0.5);

        float turbulence = fbm(polarCoord + time * 0.1);

        float temp = 1.0 - (dist - diskInner) / (diskOuter - diskInner);

        vec3 hotColor = mix(
            vec3(1.0, 0.3, 0.0),
            vec3(1.0, 0.9, 0.4),
            temp * temp
        );

        hotColor *= (0.7 + 0.3 * turbulence);

        float emission = (1.5 + 0.3 * sin(time * 0.3)) * temp;

        color = hotColor * emission * diskMask;
        alpha = diskMask;
    }
    else if(dist < diskInner)
    {
        float lensZone = (dist - eventHorizon) / (diskInner - eventHorizon);

        vec2 distortedUV = uv * (1.0 + 0.3 * (1.0 - lensZone));
        float distortNoise = fbm(distortedUV * 5.0 + time * 0.05);

        vec3 lensGlow = vec3(0.8, 0.4, 0.1) * distortNoise * 0.3;

        color = lensGlow;
        alpha = 0.5 + 0.3 * distortNoise;
    }

    if(dist > eventHorizon - 0.05 && dist < eventHorizon + 0.05) {
        float hawkingGlow = smoothstep(eventHorizon - 0.05, eventHorizon, dist) *
        smoothstep(eventHorizon + 0.05, eventHorizon, dist);
        vec3 hawkingColor = vec3(0.4, 0.6, 1.0) * 0.5;
        color += hawkingColor * hawkingGlow;
    }

    float flicker = 0.95 + 0.05 * sin(time * 5.0 + dist * 10.0);
    color *= flicker;

    vec4 finalColor = vec4(color * 2.0, alpha) * vertexColor * ColorModulator;

    float fogValue = smoothstep(FogStart, FogEnd, vertexDistance);
    finalColor.rgb = mix(finalColor.rgb, FogColor.rgb, fogValue * FogColor.a);

    fragColor = finalColor;
}
