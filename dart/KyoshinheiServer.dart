#import("dart:io");
#source("Command.dart");
#source("Config.dart");
#source("SocketServerFactory.dart");

SocketServerFactory socket;

bool status = false;

void main() {
  // Socket を作成
  socket = new SocketServerFactory();

  // HTTP Server 
  startHttpServer();
}

void startHttpServer() {
  HttpServer server = new HttpServer();
  
  server.onRequest = (HttpRequest req, HttpResponse rsp) {
    requestReceivedHandler(req, rsp);
  };
  server.listen(Config.HTTP_HOST, Config.HTTP_PORT);
  
  if (Config.LOG) {
    print("Serving the current time on http://${Config.HTTP_HOST}:${Config.HTTP_PORT}."); 
  }
}

void requestReceivedHandler(HttpRequest request, HttpResponse response) {
  if (Config.LOG) {
    print("Request: ${request.method} ${request.uri}");
  }

  String htmlResponse = createHtmlResponse(status);

  response.setHeader("Content-Type", "text/html; charset=UTF-8");
  response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:3030");
  response.setHeader("Access-Control-Allow-Credentials", "true");
  response.outputStream.writeString(htmlResponse);
  response.outputStream.close();
}

String createHtmlResponse(bool b) {
  if(b) {
  return 
'''
{
  "status": "on"
}
''';
  }
  else {
'''
{
  "status": "off"
}
''';
  }
}