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
    for (int i = 0; i < 6; ++i)
    {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

float ringMask(float d, float r, float thickness)
{
    return smoothstep(r - thickness, r, d) * (1.0 - smoothstep(r, r + thickness, d));
}

void main()
{
    vec2 flipped = vec2(1.225 - texCoord0.x, 0.75 - (texCoord0.y / 2.0));
    vec2 centered = (flipped - 0.5) * 2.0;
    float dist = length(centered);

    float angle = atan(centered.y, centered.x);
    float aNorm = (angle + PI) / (2.0 * PI);
    float eventHorizon = 0.15;
    float diskInner = 0.10;
    float diskOuter = 1.00;
    float t = GameTime * 4000;

    vec3 color = vec3(0.0);
    float alpha = 1.0;

    if (dist < eventHorizon)
    {
        color = vec3(0.0);
        alpha = 1.0;
        color += 0.06 * (0.7 + 0.3 * sin(t * 6.0)) * vec3(0.2, 0.4, 0.9);

        float rimThickness = 0.025;
        float rimDist = abs(dist - eventHorizon);
        float rimMask = smoothstep(rimThickness, 0.0, rimDist);
        vec3 rimColor = vec3(1.0, 1.0, 1.0) * 8.0;
        color += rimColor * rimMask;
    } else if (dist >= diskInner && dist <= diskOuter)
    {
        float radial = clamp(1.0 - (dist - diskInner) / max(0.0001, diskOuter - diskInner), 0.0, 1.0);

        float rings = 0.0;
        rings += 0.6 * ringMask(dist, mix(diskInner, diskOuter, 0.15), 0.04) * (0.8 + 0.4 * fbm(vec2(dist * 8.0, t * 0.3)));
        rings += 0.45 * ringMask(dist, mix(diskInner, diskOuter, 0.35), 0.06) * (0.6 + 0.6 * fbm(vec2(dist * 6.0 + 5.0, t * 0.25)));
        rings += 0.3  * ringMask(dist, mix(diskInner, diskOuter, 0.60), 0.08) * (0.5 + 0.6 * fbm(vec2(dist * 4.0 + 12.0, t * 0.2)));

        float armCount = 3.0;
        float armTightness = 3.5;
        float spiralRaw = sin( armCount * angle - armTightness * log(max(dist, 0.001)) - t * 1.8 );
        float spiralMask = 1.0 - smoothstep(0.0, 0.8, abs(spiralRaw));
        float spiralTurb = fbm(vec2(aNorm * 20.0, dist * 6.0 + t * 0.8));
        spiralMask *= (0.6 + 0.6 * spiralTurb);

        float clump = fbm(vec2(aNorm * 40.0 + t * 0.4, dist * 20.0 + t * 0.6));
        clump *= pow(radial, 0.8);

        float speckNoise = fbm(vec2(aNorm * 200.0 + t * 3.0, dist * 200.0));
        float specks = step(0.92, speckNoise) * smoothstep(0.92, 1.0, speckNoise);
        specks *= 1.5 * radial;

        float streak = pow(max(0.0, 1.0 - dist / (diskOuter)), 2.0) *
        (0.4 + 0.8 * fbm(vec2(angle * 8.0 + t * 2.0, dist * 10.0)));
        float spokeRaw = sin(angle * 30.0 - t * 4.0);
        float spoke = 1.0 - smoothstep(0.0, 0.5, abs(spokeRaw));

        float diskL = clamp( 1.2 * radial * rings + 0.9 * spiralMask + 0.6 * clump + 2.2 * specks + 0.6 * streak * spoke, 0.0, 4.0);

        vec3 innerColor = vec3(1.0, 0.9, 0.4);
        vec3 midColor   = vec3(1.0, 0.3, 0.7);
        vec3 outerColor = vec3(0.2, 0.6, 1.0);

        float blendMid = smoothstep(0.0, 0.6, radial);
        vec3 diskColor = mix(outerColor, mix(midColor, innerColor, pow(radial, 1.5)), blendMid);

        color += diskColor * diskL * 0.9;
        alpha = clamp(0.2 + diskL * 0.6, 0.0, 1.0);
    } else if (dist < diskInner) {
        float lensZone = clamp((dist - eventHorizon) / max(0.0001, (diskInner - eventHorizon)), 0.0, 1.0);
        vec2 distortedUV = centered * (1.0 + 0.45 * (1.0 - lensZone));
        float distortNoise = fbm(distortedUV * 6.0 + t * 0.9);

        vec3 lensGlow = mix(vec3(0.6,0.4,1.0), vec3(0.9,0.7,0.5), 0.5 + 0.5 * distortNoise) * 0.9;
        color = lensGlow * (0.6 + 0.8 * distortNoise) * (1.0 - lensZone);
        alpha = 0.25 + 0.6 * distortNoise * (1.0 - lensZone);
    }

    {
        float thickness = 0.06;
        float edge = abs(dist - eventHorizon);
        float hawkingGlow = smoothstep(thickness, 0.0, edge);
        vec3 hawkingColor = vec3(0.5, 0.75, 1.0) * (0.8 + 0.2 * sin(t * 3.5));
        color += hawkingColor * hawkingGlow * 0.9;

        float rimStreaks = 0.25 * smoothstep(0.02, 0.0, edge) * (0.6 + 0.5 * fbm(vec2(angle * 40.0, t * 2.0)));
        color += vec3(1.0, 0.8, 0.7) * rimStreaks;
    }

    float flicker = 0.96 + 0.08 * sin(t * 6.0 + dist * 10.0);
    color *= flicker;

    vec4 finalColor = vec4(color, alpha) * vertexColor * ColorModulator;
    float fogValue = smoothstep(FogStart, FogEnd, vertexDistance);
    finalColor.rgb = mix(finalColor.rgb, FogColor.rgb, fogValue * FogColor.a);

    if (finalColor.a <= 0.001) discard;
    fragColor = finalColor;
}
