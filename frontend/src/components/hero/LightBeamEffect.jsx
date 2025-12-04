import React, { useEffect, useRef } from 'react';

const LightBeamEffect = () => {
    const containerRef = useRef(null);
    const sceneRef = useRef(null);
    const rendererRef = useRef(null);
    const cameraRef = useRef(null);
    const beamsRef = useRef([]);
    const waveRef = useRef(null);
    const animationFrameRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        // Kiểm tra Three.js có sẵn không
        if (typeof THREE === 'undefined') {
            console.error('Three.js is not loaded. Please install it: npm install three');
            return;
        }

        // Tạo scene, camera, renderer
        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(
            75,
            window.innerWidth / window.innerHeight,
            0.1,
            1000
        );
        camera.position.z = 5;

        const renderer = new THREE.WebGLRenderer({
            alpha: true,
            antialias: true,
        });
        renderer.setSize(window.innerWidth, window.innerHeight);
        renderer.setPixelRatio(window.devicePixelRatio);
        containerRef.current.appendChild(renderer.domElement);

        sceneRef.current = scene;
        rendererRef.current = renderer;
        cameraRef.current = camera;

        // Tạo Light Beams (Tia sáng)
        const beams = [];
        const beamCount = 6; // Giảm số lượng để tối ưu performance

        for (let i = 0; i < beamCount; i++) {
            // Tạo geometry cho beam (hình trụ dẹt)
            const geometry = new THREE.PlaneGeometry(0.15, 5);

            // Tạo material với gradient và trong suốt
            const material = new THREE.ShaderMaterial({
                transparent: true,
                blending: THREE.AdditiveBlending,
                side: THREE.DoubleSide,
                depthWrite: false,
                uniforms: {
                    time: { value: 0 },
                    color: { value: new THREE.Color(0x5a9ff5) },
                    opacity: { value: 0.5 },
                },
                vertexShader: `
                    varying vec2 vUv;
                    void main() {
                        vUv = uv;
                        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                    }
                `,
                fragmentShader: `
                    uniform float time;
                    uniform vec3 color;
                    uniform float opacity;
                    varying vec2 vUv;
                    
                    void main() {
                        // Tạo gradient từ trung tâm ra ngoài
                        float dist = abs(vUv.y - 0.5) * 2.0;
                        float alpha = (1.0 - dist) * opacity;
                        
                        // Thêm hiệu ứng nhấp nháy mượt mà
                        float pulse = sin(time * 1.5 + vUv.x * 8.0) * 0.25 + 0.75;
                        alpha *= pulse;
                        
                        // Tạo màu gradient với màu xanh dương và tím
                        vec3 finalColor = color;
                        finalColor += vec3(0.4, 0.6, 1.0) * (1.0 - dist * 0.5);
                        finalColor += vec3(0.8, 0.5, 1.0) * sin(time + vUv.y * 5.0) * 0.2;
                        
                        gl_FragColor = vec4(finalColor, alpha);
                    }
                `,
            });

            const beam = new THREE.Mesh(geometry, material);

            // Đặt vị trí theo vòng tròn
            const angle = (i / beamCount) * Math.PI * 2;
            const radius = 2.5 + Math.random() * 1.5;
            beam.position.x = Math.cos(angle) * radius;
            beam.position.y = Math.sin(angle) * radius * 0.6; // Ép dẹt theo chiều dọc
            beam.rotation.z = angle + Math.PI / 2;

            // Thêm animation properties
            beam.userData = {
                speed: 0.3 + Math.random() * 0.3,
                rotationSpeed: 0.008 + Math.random() * 0.015,
                radius: radius,
                angle: angle,
                verticalOffset: (Math.random() - 0.5) * 1.5,
            };

            scene.add(beam);
            beams.push(beam);
        }

        beamsRef.current = beams;

        // Tạo Energy Wave (Sóng năng lượng) - Tối ưu hóa với ít segments hơn
        const waveGeometry = new THREE.PlaneGeometry(10, 10, 24, 24);
        const waveMaterial = new THREE.ShaderMaterial({
            transparent: true,
            blending: THREE.AdditiveBlending,
            side: THREE.DoubleSide,
            depthWrite: false,
            uniforms: {
                time: { value: 0 },
                color: { value: new THREE.Color(0x7b68ee) },
            },
            vertexShader: `
                uniform float time;
                varying vec3 vPosition;
                varying vec2 vUv;
                
                void main() {
                    vUv = uv;
                    vPosition = position;
                    
                    // Tạo sóng phức tạp hơn
                    vec3 pos = position;
                    float wave = sin(pos.x * 1.5 + time * 1.8) * 0.25;
                    wave += sin(pos.y * 1.5 + time * 1.2) * 0.2;
                    wave += sin(pos.x * 3.0 + pos.y * 3.0 + time * 2.5) * 0.1;
                    pos.z += wave;
                    
                    gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
                }
            `,
            fragmentShader: `
                uniform float time;
                uniform vec3 color;
                varying vec3 vPosition;
                varying vec2 vUv;
                
                void main() {
                    // Tạo pattern sóng phức tạp
                    float wave1 = sin(vPosition.x * 2.5 + time * 1.8) * 0.5 + 0.5;
                    float wave2 = sin(vPosition.y * 2.5 + time * 1.3) * 0.5 + 0.5;
                    float wave3 = sin(vPosition.x * 4.0 + vPosition.y * 4.0 + time * 2.2) * 0.3 + 0.7;
                    float pattern = wave1 * wave2 * wave3;
                    
                    // Tạo gradient từ trung tâm với falloff mượt
                    float dist = length(vUv - 0.5) * 2.0;
                    float alpha = pow(1.0 - dist, 2.0) * 0.25 * pattern;
                    
                    // Màu năng lượng với gradient
                    vec3 finalColor = color;
                    finalColor += vec3(0.3, 0.5, 1.0) * pattern * 0.8;
                    finalColor += vec3(0.9, 0.6, 1.0) * sin(time + dist * 3.0) * 0.3;
                    
                    gl_FragColor = vec4(finalColor, alpha);
                }
            `,
        });

        const wave = new THREE.Mesh(waveGeometry, waveMaterial);
        wave.position.z = -1.5;
        wave.rotation.x = -Math.PI / 5;
        scene.add(wave);
        waveRef.current = wave;

        // Animation loop
        let time = 0;
        const animate = () => {
            animationFrameRef.current = requestAnimationFrame(animate);
            time += 0.016; // ~60fps

            // Cập nhật beams
            if (beamsRef.current && beamsRef.current.length > 0) {
                beamsRef.current.forEach((beam, index) => {
                    const { rotationSpeed, radius, verticalOffset } = beam.userData;

                    // Xoay beams quanh trung tâm
                    beam.userData.angle += rotationSpeed;
                    const angle = beam.userData.angle;
                    beam.position.x = Math.cos(angle) * radius;
                    beam.position.y = Math.sin(angle) * radius * 0.6 + verticalOffset + Math.sin(time * 0.5 + index) * 0.3;
                    beam.rotation.z = angle + Math.PI / 2;

                    // Thêm hiệu ứng scale nhẹ
                    const scale = 1 + Math.sin(time * 2 + index) * 0.1;
                    beam.scale.y = scale;

                    // Cập nhật shader time
                    beam.material.uniforms.time.value = time;
                });
            }

            // Cập nhật wave
            if (waveRef.current) {
                waveRef.current.material.uniforms.time.value = time;
                waveRef.current.rotation.z += 0.002;
            }

            renderer.render(scene, camera);
        };

        animate();

        // Handle resize
        const handleResize = () => {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        };
        window.addEventListener('resize', handleResize);

        // Cleanup
        return () => {
            window.removeEventListener('resize', handleResize);
            if (animationFrameRef.current) {
                cancelAnimationFrame(animationFrameRef.current);
            }
            const renderer = rendererRef.current;
            if (containerRef.current && renderer && renderer.domElement) {
                try {
                    containerRef.current.removeChild(renderer.domElement);
                } catch (e) {
                    console.warn('Error removing renderer DOM element:', e);
                }
            }
            if (renderer) {
                renderer.dispose();
            }
            if (beamsRef.current && beamsRef.current.length > 0) {
                beamsRef.current.forEach(beam => {
                    if (beam.geometry) beam.geometry.dispose();
                    if (beam.material) beam.material.dispose();
                });
                beamsRef.current = [];
            }
            if (waveRef.current) {
                if (waveRef.current.geometry) waveRef.current.geometry.dispose();
                if (waveRef.current.material) waveRef.current.material.dispose();
                waveRef.current = null;
            }
        };
    }, []);

    return (
        <div
            ref={containerRef}
            className="light-beam-effect"
            style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                zIndex: 1,
                pointerEvents: 'none',
            }}
        />
    );
};

export default LightBeamEffect;