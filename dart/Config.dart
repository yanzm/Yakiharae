class Config {
  // arduino socket config
  static final String SOCKET_IP = "192.168.100.100";
  static final int SOCKET_PORT = 10002;
  
  // http servlet config
  static final String HTTP_HOST = "127.0.0.1";
  static final int HTTP_PORT = 8080;
  
  // log flag
  static final bool LOG = true;
  
  // tests without arduino connect
  static final bool ARDUINO_DISCONNECTED = false;
  
  // bell off
  static final bool ARDUINO_BELL_OFF = false;
}