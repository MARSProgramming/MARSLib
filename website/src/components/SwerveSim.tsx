import React, { useEffect, useRef } from 'react';

export default function SwerveSim() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = 0;
    let height = 0;

    const resize = () => {
      const parent = canvas.parentElement;
      if (parent) {
        width = parent.clientWidth;
        height = parent.clientHeight || 400; // default height if unspecified
        canvas.width = width;
        canvas.height = height;
      }
    };
    window.addEventListener('resize', resize);
    resize();

    // Robot state
    let x = width / 2;
    let y = height / 2;
    let heading = 0; // radians
    let vx = 0;
    let vy = 0;
    let omega = 0;

    // Trajectory generation (Lissajous curve / figure 8)
    const centerX = width / 2;
    const centerY = height / 2;
    const amplitudeX = Math.min(width * 0.35, 250);
    const amplitudeY = Math.min(height * 0.35, 150);
    
    let time = 0;
    
    const history: {x: number, y: number}[] = [];

    const dt = 0.02; // 50hz (20ms)

    function loop() {
      // Calculate target position on figure 8
      const targetX = centerX + amplitudeX * Math.sin(time * 0.5);
      const targetY = centerY + amplitudeY * Math.sin(time);
      
      const targetDX = amplitudeX * 0.5 * Math.cos(time * 0.5);
      const targetDY = amplitudeY * Math.cos(time);

      const targetHeading = Math.atan2(targetDY, targetDX);
      
      // Control loops (P-controller for chasing the target)
      vx = (targetX - x) * 2.0;
      vy = (targetY - y) * 2.0;
      
      let headingError = targetHeading - heading;
      // Normalize to -PI to PI
      while(headingError > Math.PI) headingError -= 2 * Math.PI;
      while(headingError < -Math.PI) headingError += 2 * Math.PI;
      
      omega = headingError * 3.0;

      // Update state
      x += vx * dt;
      y += vy * dt;
      heading += omega * dt;
      time += dt;

      history.push({x, y});
      if (history.length > 200) history.shift();

      draw();
      requestAnimationFrame(loop);
    }

    function draw() {
      ctx.clearRect(0, 0, width, height);

      // Draw Grid / Field
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.05)';
      ctx.lineWidth = 1;
      const gridSize = 40;
      for(let i = 0; i < width; i += gridSize) {
        ctx.beginPath(); ctx.moveTo(i, 0); ctx.lineTo(i, height); ctx.stroke();
      }
      for(let i = 0; i < height; i += gridSize) {
        ctx.beginPath(); ctx.moveTo(0, i); ctx.lineTo(width, i); ctx.stroke();
      }

      // Draw Trajectory History
      if (history.length > 1) {
        ctx.beginPath();
        ctx.strokeStyle = 'rgba(41, 182, 246, 0.4)'; // AI Cyan
        ctx.lineWidth = 3;
        ctx.moveTo(history[0].x, history[0].y);
        for(let i = 1; i < history.length; i++) {
          ctx.lineTo(history[i].x, history[i].y);
        }
        ctx.stroke();
      }

      const rbW = 60;
      const rbH = 60;

      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(heading);

      // Chassis
      ctx.fillStyle = 'rgba(20, 20, 20, 0.9)';
      ctx.strokeStyle = '#B32416'; // MARS Red
      ctx.lineWidth = 2;
      ctx.fillRect(-rbW/2, -rbH/2, rbW, rbH);
      ctx.strokeRect(-rbW/2, -rbH/2, rbW, rbH);

      // Modules
      const mR = 8;
      const positions = [
        [-rbW/2, -rbH/2],
        [rbW/2, -rbH/2],
        [-rbW/2, rbH/2],
        [rbW/2, rbH/2]
      ];

      // Swerve Kinematics math for module angles
      // In chassis frame
      const vxChassis = vx * Math.cos(-heading) - vy * Math.sin(-heading);
      const vyChassis = vx * Math.sin(-heading) + vy * Math.cos(-heading);

      positions.forEach(pos => {
        // Module velocity component from chassis rotation
        const mx = vxChassis - omega * pos[1];
        const my = vyChassis + omega * pos[0];
        const mAngle = Math.atan2(my, mx);
        const mSpeed = Math.sqrt(mx*mx + my*my);

        ctx.save();
        ctx.translate(pos[0], pos[1]);
        
        ctx.fillStyle = '#222';
        ctx.beginPath(); ctx.arc(0, 0, mR, 0, Math.PI * 2); ctx.fill();
        
        ctx.rotate(mAngle);
        ctx.fillStyle = '#29b6f6'; // Cyan wheel
        ctx.fillRect(-mR, -2, mR * 2, 4);
        
        // Velocity vector
        if (mSpeed > 10) {
           ctx.strokeStyle = '#9c7bcc'; // Purple vector
           ctx.lineWidth = 2;
           ctx.beginPath();
           ctx.moveTo(0,0);
           ctx.lineTo(mSpeed * 0.2, 0);
           ctx.stroke();
        }

        ctx.restore();
      });

      // Direction indicator
      ctx.fillStyle = '#d42e1e';
      ctx.beginPath(); ctx.arc(rbW/2, 0, 4, 0, Math.PI * 2); ctx.fill();

      ctx.restore();

      // UI Overlay
      ctx.fillStyle = 'rgba(255,255,255,0.7)';
      ctx.font = '12px "Orbitron", sans-serif';
      ctx.fillText(`X: ${x.toFixed(1)}`, 20, 30);
      ctx.fillText(`Y: ${y.toFixed(1)}`, 20, 50);
      ctx.fillText(`Î¸: ${(heading * 180 / Math.PI).toFixed(1)}Â°`, 20, 70);
    }
    
    // Allow the div to resize before grabbing width
    setTimeout(() => {
        resize();
        loop();
    }, 100);

    return () => {
      window.removeEventListener('resize', resize);
    };
  }, []);

  return (
    <div style={{ width: '100%', height: '400px', backgroundColor: '#0a0a0a', border: '1px solid #2a2a2a', borderRadius: '8px', overflow: 'hidden', position: 'relative' }}>
      <canvas ref={canvasRef} style={{ display: 'block', width: '100%', height: '100%' }} />
      <div style={{ position: 'absolute', bottom: '15px', right: '15px', padding: '5px 12px', background: 'rgba(179,36,22,0.8)', color: '#fff', fontSize: '10px', fontFamily: '"Orbitron", sans-serif', letterSpacing: '0.1em', borderRadius: '4px' }}>
          LIVE ODOMETRY SIM
      </div>
    </div>
  );
}
