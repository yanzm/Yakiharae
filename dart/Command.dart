class Command {
  static String ACK = "ACK\n";
  static String TIMEOUT = "T\n";
  static String TURN_ON_LEDS = "L";
  static String BUTTON_SELECTED = "O";
  static String BELL = "S";
  
  static String TURN_ON(String button, String rgb)  {
    return "${TURN_ON_LEDS}${button}${rgb}\n";
  }
  
  static String TURN_BELL_ON(int times)  {
    return "${BELL}${times}\n";
  }  
  
  static bool isButtonSelected(String text)  {
    if (text.substring(0,1) == BUTTON_SELECTED) {
      return true;
    }
    
    return false;
  }
  
}