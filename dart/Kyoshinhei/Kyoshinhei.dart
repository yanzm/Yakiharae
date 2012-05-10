#import('dart:html');
#import('dart:json');
#source("Config.dart");

final WIDTH = 1280;
final HEIGHT = 720;
final BASE = 100;

bool started = false;

var handle;
var loadHandle;

CanvasRenderingContext2D ctx;

int x = 0;
int y = 0;
int blastWidth = 0;

void updateTime() {
  if(x < WIDTH) {
    drawLight();
  }
  else if(y < WIDTH){
    drawExplosion();    
  }
  else {
    drawBlast();
  }
}

void drawBlast() {  
  ctx.clearRect(0, 0, WIDTH, HEIGHT);
  
  ctx.fillStyle = "9F0000";
  ctx.fillRect(0, 0, WIDTH, HEIGHT - BASE);
  
  double x1 = WIDTH / 2 - 100;
  double y1 = HEIGHT - BASE - 100;
  
  var grad1 = ctx.createRadialGradient(x1, y1, blastWidth * 0.8, x1, y1, blastWidth);
  grad1.addColorStop(0,'#EF7319'); // オレンジ
  grad1.addColorStop(1,'#9F0000'); // 赤

  double x2 = WIDTH / 2 + 400;
  double y2 = HEIGHT - BASE - 200;

  var grad2 = ctx.createRadialGradient(x2, y2, blastWidth * 0.8, x2, y2, blastWidth);
  grad2.addColorStop(0,'#EF7319'); // オレンジ
  grad2.addColorStop(1,'#9F0000'); // 赤

  double x3 = WIDTH / 2 - 300;
  double y3 = HEIGHT - BASE - 100;

  var grad3 = ctx.createRadialGradient(x3, y3, blastWidth * 0.8, x3, y3, blastWidth);
  grad3.addColorStop(0,'#EF7319'); // オレンジ
  grad3.addColorStop(1,'#9F0000'); // 赤

  ctx.fillStyle = grad1;
  ctx.beginPath();
  ctx.arc(x1, y1, blastWidth, Math.PI * 2, 0, false);
  ctx.fill();
  ctx.closePath();

  ctx.fillStyle = grad2;
  ctx.beginPath();
  ctx.arc(x2, y2, blastWidth, Math.PI * 2, 0, false);
  ctx.fill();
  ctx.closePath();

  ctx.fillStyle = grad3;
  ctx.beginPath();
  ctx.arc(x3, y3, blastWidth, Math.PI * 2, 0, false);
  ctx.fill();
  ctx.closePath();
  
  
  // ground
  var grad4 = ctx.createRadialGradient(WIDTH / 2, HEIGHT / 2, WIDTH / 4, WIDTH / 2, HEIGHT / 2, WIDTH / 2);
  grad4.addColorStop(0,'#855D46');
  grad4.addColorStop(1,'#400E12');

  ctx.fillStyle = grad4;  
  ctx.fillRect(0, HEIGHT - BASE, WIDTH, HEIGHT);
  
  
  blastWidth += 5;
  if(blastWidth > WIDTH / 2) {
    blastWidth = 0;
    window.clearInterval(handle);
  }
}

void drawExplosion() {
  ctx.clearRect(0, 0, WIDTH, HEIGHT);

  var grad1 = ctx.createRadialGradient(WIDTH / 2, HEIGHT - BASE, y * 0.8, WIDTH / 2, HEIGHT - BASE, y);
  grad1.addColorStop(0,'white');      // 赤
  grad1.addColorStop(1,'black');
  ctx.beginPath();
  ctx.arc(WIDTH / 2, HEIGHT - BASE, y, Math.PI, 0, false);
  ctx.fill();
  ctx.closePath();

  if(y >= 100) {
    var grad = ctx.createRadialGradient(WIDTH / 2, HEIGHT - BASE, (y - 100) * 0.8, WIDTH / 2, HEIGHT - BASE, y - 100);
    grad.addColorStop(0,'#9F0000');      // 赤
    grad.addColorStop(1,'white');

    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(WIDTH / 2, HEIGHT - BASE, y - 100, Math.PI, 0, false);
    ctx.fill();
    ctx.closePath();
  }
  
  // ground
  var grad4 = ctx.createRadialGradient(WIDTH / 2, HEIGHT / 2, WIDTH / 4, WIDTH / 2, HEIGHT / 2, WIDTH / 2);
  grad4.addColorStop(0,'#855D46');
  grad4.addColorStop(1,'#400E12');

  ctx.fillStyle = grad4;  
  ctx.fillRect(0, HEIGHT - BASE, WIDTH, HEIGHT);

  y += 20;
  if(y > WIDTH) {
    y = 0;
  }

}

void drawLight() {
  ctx.clearRect(0, 0, WIDTH, HEIGHT);
  
  var grad = ctx.createRadialGradient(x, HEIGHT - BASE, 0, x, HEIGHT - BASE, 40);
  grad.addColorStop(0,'#9F0000');      // 赤
  grad.addColorStop(1,'black');

  ctx.fillStyle = grad;
  ctx.beginPath();
  ctx.arc(x, HEIGHT - BASE, 40, Math.PI * 2, 0, false);
  ctx.fill();
  ctx.closePath();

  ctx.strokeStyle = "white";
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(x, HEIGHT - BASE);
  ctx.closePath();
  ctx.stroke();
  
  x += 20;
  if(x > WIDTH) {
    x = 0;
  }
}

void loadStatus() {
  XMLHttpRequest request = new XMLHttpRequest();
  request.withCredentials = true;
  request.on.load.add((e) { 
    handleStatus(request); 
  });
  
  request.open('GET', "http://${Config.HTTP_HOST}:${Config.HTTP_PORT}/", true);
  request.send();
}

void handleStatus(XMLHttpRequest request) {
  Map json = JSON.parse(request.responseText);
  
  var status = json["status"];
  
  if (status == "on" && started == false) {
    started = true;
    window.clearInterval(loadHandle);
    handle = window.setInterval(f() => updateTime(), 5);
  }
}

void main() {  
  // canvas を取得
  CanvasElement canvas = document.query("#canvas");
  ctx = canvas.getContext("2d");
  ctx.fillStyle = "black";
  ctx.fillRect(0, 0, WIDTH, HEIGHT);
  
  loadHandle = window.setInterval(loadStatus, 500);
}
