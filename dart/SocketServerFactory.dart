class SocketServerFactory {

  ServerSocket serverSocket;
  Socket socket;
  SocketInputStream inputStream;
  OutputStream outputStream;
  StringBuffer stringBuffer;
  
  SocketServerFactory()  {
    
    // Socket を作成
    serverSocket = new ServerSocket(Config.SOCKET_IP, Config.SOCKET_PORT, 5);

    //　エラーハンドラ
    serverSocket.onError = (e) => print("Error:" + e);

    // コネクトハンドラ
    serverSocket.onConnection = (connection) {
      // 受信して読んでないバイト数
      print("Connected:");

      socket = connection;
      
      // Socket から入力ストリームを取得
      inputStream = socket.inputStream;

      // データハンドラ
      inputStream.onData = () {
        // 入力ストリームから読み込み
        List<int> data = inputStream.read();
        if(data != null) {
          String command = new String.fromCharCodes(data);
          receiveCommand(command);
        }    
      }; 
      
      // クローズハンドラ
      inputStream.onClosed = () {
        socket.close();
        serverSocket.close();
        print("Closed:");
      };
    
      outputStream = socket.outputStream;

      write(Command.ACK);
    };
  }
  
  /**
   * Socket write
   **/
  void write(String command)  {
    if (Config.LOG)
      print("socketCommand => ${command}");
    
    if (outputStream != null)
      outputStream.writeString(command);
  }
  
  void receiveCommand(String text) {  
    if (Config.LOG) {
      print("receiveCommand => ${text}");
    }

    status = true;
    
    new Timer(2000, (var finishSeconds) {
      status = false;
    });
    
    write(Command.ACK);
  }  
}
