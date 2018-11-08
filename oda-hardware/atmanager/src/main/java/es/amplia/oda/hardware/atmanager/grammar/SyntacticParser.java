package es.amplia.oda.hardware.atmanager.grammar;

public class SyntacticParser {
    private String toParse;
    private int index;
    private Tokens current;
    private String lastCommand;
    private String lastConstant;
    private int lastNumber;
    private double lastFloat;
    private String lastIPAddress;
    private String lastString;

    SyntacticParser(String toParse) {
        this.toParse = toParse;
        this.index = 0;
        advance();
    }

    void advance() {
        if (atEnd()) {
            current = Tokens.EOF;
            return;
        }
        char c = currentChar();
        if (c == 'S') {
            index++;
            if (atEnd()) {
                current = Tokens.ERROR;
                return;
            }
            c = currentChar();
            if (c < '0' || c > '9') {
                current = Tokens.ERROR;
                return;
            }
            current = Tokens.S_NAME;
            lastCommand = "S" + c;
            while (true) {
                if (atEnd()) {
                    return;
                }
                index++;
                c = currentChar();
                if (c < '0' || c > '9') {
                    return;
                }
                lastCommand = lastCommand + c;
            }
        }
        if (c >= 'A' && c <= 'Z') {
            current = Tokens.BASIC_NAME;
            lastCommand = "" + c;
            lastConstant = "" + c;
            while (true) {
                index++;
                if (atEnd()) {
                    break;
                }
                c = currentChar();
                if (c >= 'A' && c <= 'Z') {
                    lastConstant = lastConstant + c;
                } else {
                    break;
                }
            }
            if (lastConstant.length() > 1) {
                current = Tokens.CONSTANT;
                lastCommand = null;
            } else {
                lastConstant = null;
            }
            return;
        }
        if (c == '&') {
            index++;
            if (!atEnd()) {
                c = currentChar();
                if (c >= 'A' && c <= 'Z') {
                    index++;
                    current = Tokens.BASIC_NAME;
                    lastCommand = "&" + c;
                    return;
                }
            }
            current = Tokens.ERROR;
            return;
        }
        if (c >= '0' && c <= '9') {
            current = Tokens.NUMBER;
            lastNumber = c - '0';
            while (true) {
                index++;
                if (atEnd()) {
                    return;
                }
                c = currentChar();
                if (c == '.') {
                    index++;
                    if (atEnd()) {
                        current = Tokens.ERROR;
                        return;
                    }
                    c = currentChar();
                    if (c < '0' || c > '9') {
                        current = Tokens.ERROR;
                        return;
                    }
                    current = Tokens.FLOAT;
                    lastFloat = lastNumber + (c - '0') * Math.pow(10,-1);
                    int decimals = 2;
                    while (true) {
                        index++;
                        if (atEnd()) {
                            return;
                        }
                        c = currentChar();
                        if (c == '.') {
                            int octets = 3;
                            lastIPAddress = lastFloat + ".";
                            while (true) {
                                index++;
                                if (atEnd()) {
                                    break;
                                }
                                c = currentChar();
                                if (c == '.')
                                    octets++;
                                else if (c < '0' || c > '9') {
                                    break;
                                }
                                lastIPAddress = lastIPAddress + c;
                            }
                            if (octets == 4) {
                                current = Tokens.IPV4;
                                return;
                            } else {
                                current = Tokens.ERROR;
                                return;
                            }
                        } else if (c < '0' || c > '9') {
                            return;
                        }
                        lastFloat = lastFloat + (c - '0') * Math.pow(10,(decimals++)*-1);
                    }
                } else if (c < '0' || c > '9') {
                    return;
                }
                lastNumber = lastNumber * 10 + c - '0';
            }
        }
        String namePrefixCharacters = "+";
        if (namePrefixCharacters.contains(c + "")) {
            index++;
            if (atEnd()) {
                current = Tokens.ERROR;
                return;
            }
            c = currentChar();
            if (!isExtendedNameChar(c)) {
                current = Tokens.ERROR;
                return;
            }
            current = Tokens.EXTENDED_NAME;
            lastCommand = "+" + c;
            while (true) {
                index++;
                if (atEnd()) {
                    return;
                }
                c = currentChar();
                if (!isExtendedNameChar(c)) {
                    return;
                }
                lastCommand = lastCommand + c;
            }
        }
        if (c == '"') {
            StringBuilder str = new StringBuilder();
            current = Tokens.STRING;
            index++;
            while (true) {
                if (atEnd()) {
                    break;
                }
                c = toParse.charAt(index);
                index++;
                if (c == '"') {
                    break;
                }
                str.append(c);
            }
            lastString = str.toString();
            return;
        }
        switch (c) {
            case ';':
                current = Tokens.SEMICOLON;
                break;
            case ',':
                current = Tokens.COMMA;
                break;
            case '?':
                current = Tokens.QUESTION;
                break;
            case ' ':
                current = Tokens.SPACE;
                break;
            case '\r':
                current = Tokens.SPACE;
                break;
            case ':':
                current = Tokens.COLON;
                break;
            case '=':
                current = Tokens.EQUAL;
                index++;
                if (atEnd()) {
                    return;
                }
                c = currentChar();
                if (c == '?') {
                    current = Tokens.EQUAL_QUESTION;
                    index++;
                }
                return;
            default:
                current = Tokens.ERROR;
                return;
        }
        index++;
    }

    private boolean isExtendedNameChar(char c) {
        if (c >= 'A' && c <= 'Z') return true;
        if (c >= '0' && c <= '9') return true;
        switch (c) {
            case '!':
            case '%':
            case '-':
            case '_':
            case '.':
            case '/':
                return true;
        }
        return false;
    }

    private char currentChar() {
        char c = toParse.charAt(index);
        c = Character.toUpperCase(c);
        return c;
    }

    private boolean atEnd() {
        return index >= toParse.length();
    }

    Tokens current() {
        return current;
    }

    String getLastCommand() {
        return lastCommand;
    }

    int getLastNumber() {
        return lastNumber;
    }

    double getLastFloat() {
        return lastFloat;
    }

    String getLastString() {
        return lastString;
    }

    String getLastIPAddress() {
        return lastIPAddress;
    }

    String getLastConstant() {
        return lastConstant;
    }

    public enum Tokens {
        // A or &A
        BASIC_NAME,
        // DGRAM (an uppercase string)
        CONSTANT,
        // S1 or S13
        S_NAME,
        // +CCGT or +C1M1 or +!%-_./:     The '+' will be configurable
        EXTENDED_NAME,
        // 123
        NUMBER,
        // 123.456
        FLOAT,
        // 192.168.1.1
        IPV4,
        // ';'
        SEMICOLON,
        // ':'
        COLON,
        // ','
        COMMA,
        // '?'
        QUESTION,
        // '='
        EQUAL,
        // =?
        EQUAL_QUESTION,
        // ' ' and '\r'
        SPACE,
        // "Some text"
        STRING,
        ERROR,
        EOF
    }
}
