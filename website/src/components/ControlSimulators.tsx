import React, { useEffect } from 'react';

export default function ControlSimulators() {
  useEffect(() => {
    // 1. Elevator/PID Simulation
    const eCanvas = document.getElementById('elevatorCanvas');
    const gCanvas = document.getElementById('graphCanvas');
    if (!eCanvas || !gCanvas) return;

    const eCtx = eCanvas.getContext('2d');
    const gCtx = gCanvas.getContext('2d');
    
    const kpSlider = document.getElementById('kp');
    const kiSlider = document.getElementById('ki');
    const kdSlider = document.getElementById('kd');
    const kgSlider = document.getElementById('kg');
    const btn = document.getElementById('resetSim');
    
    if(!kpSlider || !btn) return;

    kpSlider.oninput = () => document.getElementById('kp-val').innerText = parseFloat(kpSlider.value).toFixed(2);
    kiSlider.oninput = () => document.getElementById('ki-val').innerText = parseFloat(kiSlider.value).toFixed(2);
    kdSlider.oninput = () => document.getElementById('kd-val').innerText = parseFloat(kdSlider.value).toFixed(2);
    kgSlider.oninput = () => document.getElementById('kg-val').innerText = parseFloat(kgSlider.value).toFixed(1);
    
    function resizeG() { 
        if(gCanvas.parentElement) {
            gCanvas.width = gCanvas.parentElement.clientWidth; 
            gCanvas.height = gCanvas.parentElement.clientHeight; 
        }
    }
    window.addEventListener('resize', resizeG);
    resizeG();

    let position = 0.2; 
    let velocity = 0;
    let setpoint = 0.8;
    let lastError = 0;
    let integral = 0;
    
    btn.onclick = () => { 
        setpoint = setpoint > 0.5 ? 0.2 : 0.8; 
        btn.style.transform = 'scale(0.95)';
        setTimeout(() => btn.style.transform = 'none', 100);
    };
    
    const history = [];
    let intervalId;
    let frameId;

    function simulate() {
      const kP = parseFloat(kpSlider.value);
      const kI = parseFloat(kiSlider.value);
      const kD = parseFloat(kdSlider.value);
      const kG = parseFloat(kgSlider.value);
      
      const error = setpoint - position;
      const errorRate = (error - lastError) / 0.02;
      lastError = error;
      
      integral += error * 0.02;
      if(integral > 2) integral = 2;
      if(integral < -2) integral = -2;
      
      let voltage = (kP * error * 50) + (kI * integral * 20) + (kD * errorRate * 1.5);
      voltage += kG;
      
      const GRAVITY_FORCE = -0.5; 
      const MOTOR_FORCE = (voltage * 0.1); 
      const FORCE = MOTOR_FORCE + GRAVITY_FORCE; 
      
      velocity += FORCE * 0.02; 
      velocity *= 0.88; 
      
      position += velocity * 0.02;
      
      if(position <= 0) { position = 0; velocity = 0; integral = 0; }
      if(position >= 1) { position = 1; velocity = 0; integral = 0; }
      
      history.push({p: position, s: setpoint});
      if(history.length > 300) history.shift();
    }

    function draw() {
      eCtx.clearRect(0,0,eCanvas.width,eCanvas.height);
      const eH = eCanvas.height;
      
      eCtx.fillStyle = '#222';
      eCtx.fillRect(35, 10, 10, eH-20);
      
      const trackH = eH - 40;
      const yPx = (1 - position) * trackH + 10;
      const syPx = (1 - setpoint) * trackH + 10;
      
      eCtx.strokeStyle = '#29b6f6';
      eCtx.lineWidth = 2;
      eCtx.beginPath(); eCtx.moveTo(10, syPx+10); eCtx.lineTo(70, syPx+10); eCtx.stroke();
      
      eCtx.fillStyle = '#B32416';
      eCtx.fillRect(20, yPx, 40, 20);
      eCtx.fillStyle = '#d42e1e';
      eCtx.fillRect(25, yPx+5, 30, 10);
      
      gCtx.clearRect(0,0,gCanvas.width,gCanvas.height);
      const gW = gCanvas.width;
      const gH = gCanvas.height;
      
      gCtx.strokeStyle = '#222';
      gCtx.lineWidth = 1;
      for(let i=0; i<=4; i++){ gCtx.beginPath(); gCtx.moveTo(0, i*(gH/4)); gCtx.lineTo(gW, i*(gH/4)); gCtx.stroke(); }
      
      if(history.length === 0) { frameId = requestAnimationFrame(draw); return; }
      
      const slice = gW / 300;
      
      gCtx.beginPath();
      gCtx.strokeStyle = '#29b6f6';
      gCtx.lineWidth = 2;
      for(let i=0; i<history.length; i++) {
          const x = i * slice;
          const y = (1 - history[i].s) * gH;
          if(i===0) gCtx.moveTo(x,y); else gCtx.lineTo(x,y);
      }
      gCtx.stroke();
      
      gCtx.beginPath();
      gCtx.strokeStyle = '#B32416';
      gCtx.lineWidth = 2;
      for(let i=0; i<history.length; i++) {
          const x = i * slice;
          const y = (1 - history[i].p) * gH;
          if(i===0) gCtx.moveTo(x,y); else gCtx.lineTo(x,y);
      }
      gCtx.stroke();
      
      frameId = requestAnimationFrame(draw);
    }
    
    intervalId = setInterval(simulate, 20);
    draw();

    return () => {
      window.removeEventListener('resize', resizeG);
      clearInterval(intervalId);
      cancelAnimationFrame(frameId);
    };
  }, []);

  useEffect(() => {
    // 2. Flywheel Simulation
    const wCanvas = document.getElementById('wheelCanvas');
    const fwGCanvas = document.getElementById('fwGraphCanvas');
    if (!wCanvas || !fwGCanvas) return;

    const wCtx = wCanvas.getContext('2d');
    const fwGCtx = fwGCanvas.getContext('2d');
    
    const fvKvSlider = document.getElementById('fw-kv');
    const fvKpSlider = document.getElementById('fw-kp');
    const fwSetSlider = document.getElementById('fw-set');
    const shootBtn = document.getElementById('btn-shoot');
    if(!shootBtn) return;
    
    fvKvSlider.oninput = () => document.getElementById('fw-kv-val').innerText = parseFloat(fvKvSlider.value).toFixed(2);
    fvKpSlider.oninput = () => document.getElementById('fw-kp-val').innerText = parseFloat(fvKpSlider.value).toFixed(2);
    fwSetSlider.oninput = () => document.getElementById('fw-set-val').innerText = fwSetSlider.value;
    
    let fwVel = 0;
    let fwAngle = 0;
    let fwSet = 80;
    let fwHist = [];
    
    shootBtn.onclick = () => {
        fwVel -= 40; 
        if(fwVel < 0) fwVel = 0;
        shootBtn.style.transform = 'scale(0.95)';
        setTimeout(() => shootBtn.style.transform = 'none', 100);
    };
    
    let intervalId;
    let frameId;

    function simFlywheel() {
        fwSet = parseFloat(fwSetSlider.value);
        const kV = parseFloat(fvKvSlider.value);
        const kP = parseFloat(fvKpSlider.value);
        
        const error = fwSet - fwVel;
        let voltage = (kV * fwSet) + (kP * error);
        
        if(voltage > 12) voltage = 12;
        if(voltage < 0) voltage = 0; 
        
        const ACCELERATION = (voltage * 15); 
        const DRAG = (fwVel * fwVel * 0.0005); 
        
        fwVel += (ACCELERATION - DRAG) * 0.02; 
        if(fwVel < 0) fwVel = 0;
        
        fwAngle += (fwVel * 0.02);
        
        fwHist.push({v: fwVel, s: fwSet});
        if(fwHist.length > 250) fwHist.shift();
    }
    
    function drawFlywheel() {
        wCtx.clearRect(0,0,wCanvas.width,wCanvas.height);
        const cx = wCanvas.width/2;
        const cy = wCanvas.height/2;
        
        wCtx.save();
        wCtx.translate(cx, cy);
        wCtx.rotate(fwAngle);
        
        wCtx.fillStyle = '#1a1a1a';
        wCtx.strokeStyle = '#29b6f6'; // var(--ai-cyan)
        wCtx.lineWidth = 4;
        wCtx.beginPath(); wCtx.arc(0,0, 50, 0, Math.PI*2); wCtx.fill(); wCtx.stroke();
        
        wCtx.fillStyle = '#444';
        for(let i=0; i<3; i++) {
            wCtx.rotate(Math.PI*2/3);
            wCtx.beginPath(); wCtx.arc(35, 0, 8, 0, Math.PI*2); wCtx.fill();
        }
        wCtx.restore();
        
        fwGCtx.clearRect(0,0,fwGCanvas.width,fwGCanvas.height);
        const gW = fwGCanvas.width;
        const gH = fwGCanvas.height;
        const maxV = 160;
        const slice = gW / 250;
        
        fwGCtx.strokeStyle = '#222';
        fwGCtx.lineWidth = 1;
        for(let i=0; i<=4; i++){ fwGCtx.beginPath(); fwGCtx.moveTo(0, i*(gH/4)); fwGCtx.lineTo(gW, i*(gH/4)); fwGCtx.stroke(); }
        
        if(fwHist.length < 2) { frameId = requestAnimationFrame(drawFlywheel); return; }
        
        fwGCtx.beginPath();
        fwGCtx.strokeStyle = '#29b6f6';
        fwGCtx.lineWidth = 2;
        for(let i=0; i<fwHist.length; i++) {
            const x = i * slice;
            const y = gH - (fwHist[i].s / maxV * gH);
            if(i===0) fwGCtx.moveTo(x,y); else fwGCtx.lineTo(x,y);
        }
        fwGCtx.stroke();
        
        fwGCtx.beginPath();
        fwGCtx.strokeStyle = '#B32416';
        fwGCtx.lineWidth = 2;
        for(let i=0; i<fwHist.length; i++) {
            const x = i * slice;
            const y = gH - (fwHist[i].v / maxV * gH);
            if(i===0) fwGCtx.moveTo(x,y); else fwGCtx.lineTo(x,y);
        }
        fwGCtx.stroke();
        
        frameId = requestAnimationFrame(drawFlywheel);
    }

    intervalId = setInterval(simFlywheel, 20);
    drawFlywheel();

    return () => {
      clearInterval(intervalId);
      cancelAnimationFrame(frameId);
    };
  }, []);

  useEffect(() => {
    // 3. Arm Simulation
    const aCanvas = document.getElementById('armCanvas');
    if (!aCanvas) return;

    const aCtx = aCanvas.getContext('2d');
    
    const armSetSlider = document.getElementById('arm-set');
    const armKgSlider = document.getElementById('arm-kg');
    const armKpSlider = document.getElementById('arm-kp');
    if(!armSetSlider) return;
    
    armSetSlider.oninput = () => document.getElementById('arm-set-val').innerText = armSetSlider.value + "Â°";
    armKgSlider.oninput = () => document.getElementById('arm-kg-val').innerText = parseFloat(armKgSlider.value).toFixed(2);
    armKpSlider.oninput = () => document.getElementById('arm-kp-val').innerText = parseFloat(armKpSlider.value).toFixed(2);
    
    let armAng = -45; 
    let armVel = 0;
    
    let intervalId;
    let frameId;

    function simArm() {
        const setTarget = parseFloat(armSetSlider.value);
        const kG = parseFloat(armKgSlider.value);
        const kP = parseFloat(armKpSlider.value);
        
        const error = setTarget - armAng;
        
        const radians = armAng * (Math.PI / 180);
        const cosTheta = Math.cos(radians);
        
        const ffVoltage = kG * cosTheta;
        const pidVoltage = kP * error;
        
        let voltage = ffVoltage + pidVoltage;
        
        const GRAVITY_PULL = -0.6 * cosTheta; 
        const MOTOR_PUSH = voltage * 1.0; 
        
        const accel = MOTOR_PUSH + GRAVITY_PULL;
        armVel += accel * 0.02;
        armVel *= 0.85; 
        
        armAng += armVel;
        
        const elAng = document.getElementById('v-ang');
        if(elAng) {
            elAng.innerText = armAng.toFixed(2) + "Â°";
            document.getElementById('v-cos-ang').innerText = armAng.toFixed(1);
            document.getElementById('v-cos-res').innerText = cosTheta.toFixed(3);
            document.getElementById('v-ffv').innerText = ffVoltage.toFixed(2) + "v";
            document.getElementById('v-bar').style.width = (Math.abs(cosTheta) * 100) + "%";
        }
    }
    
    function drawArm() {
        aCtx.clearRect(0,0,aCanvas.width,aCanvas.height);
        const cx = aCanvas.width/2;
        const cy = aCanvas.height/2;
        
        aCtx.fillStyle = '#444';
        aCtx.beginPath(); aCtx.arc(cx,cy, 15, 0, Math.PI*2); aCtx.fill();
        
        aCtx.strokeStyle = '#222';
        aCtx.lineWidth = 1;
        aCtx.beginPath(); aCtx.moveTo(0,cy); aCtx.lineTo(200,cy); aCtx.stroke(); 
        aCtx.beginPath(); aCtx.moveTo(cx,0); aCtx.lineTo(cx,200); aCtx.stroke(); 
        
        aCtx.save();
        aCtx.translate(cx, cy);
        aCtx.rotate(-armAng * Math.PI/180); 
        
        aCtx.fillStyle = '#B32416';
        aCtx.fillRect(0, -10, 80, 20); 
        
        aCtx.fillStyle = '#29b6f6';
        aCtx.beginPath(); aCtx.arc(0,0, 6, 0, Math.PI*2); aCtx.fill();
        
        aCtx.restore();
        
        frameId = requestAnimationFrame(drawArm);
    }
    
    intervalId = setInterval(simArm, 20);
    drawArm();

    return () => {
      clearInterval(intervalId);
      cancelAnimationFrame(frameId);
    };
  }, []);

  return null;
}
