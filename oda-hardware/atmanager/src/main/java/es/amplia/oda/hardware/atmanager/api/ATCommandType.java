package es.amplia.oda.hardware.atmanager.api;

public enum ATCommandType {
    ACTION,  ///< Action AT command                     (e.g. 'AT+CMD')
    SET,     ///< AT command with or without parameters (e.g. 'AT+CMD=21,"y"' or 'AT+CMD=')
    TEST,    ///< Test AT command                       (e.g. 'AT+CMD=?')
    READ     ///< Read AT command                       (e.g. 'AT+CMD?')
}