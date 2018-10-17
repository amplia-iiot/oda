package es.amplia.oda.hardware.atmanager.grammar;

public class SyntacticParser {
    private String toParse;
    private int index;
    private Tokens current;
    private String lastCommand;
    private String lastConstant;
    private int lastNumber;
    private String lastIPaddress;
    private String namePrefixCharacters = "+";
    private String lastString;
    public SyntacticParser(String toParse) {
        this.toParse = toParse;
        this.index = 0;
        advance();
    }

    public void advance() {
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
                if (atEnd()) return;
                index++;
                c = currentChar();
                if (c < '0' || c > '9') return;
                lastCommand = lastCommand + c;
            }
        }
        if (c >= 'A' && c <= 'Z') {
            current = Tokens.BASIC_NAME;
            lastCommand = "" + c;
            lastConstant = "" + c;
            while (true) {
                index++;
                if (atEnd()) break;
                c = currentChar();
                if (c >= 'A' && c <= 'Z') {
                    lastConstant = lastConstant + c;
                } else
                    break;
            }
            if (lastConstant.length() > 1) {
                current = Tokens.CONSTANT;
                lastCommand = null;
            } else
                lastConstant = null;
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
                if (atEnd()) return;
                c = currentChar();
                if (c == '.') {
                    int octets = 2;
                    lastIPaddress = lastNumber + ".";
                    while (true) {
                        index++;
                        if (atEnd()) break;
                        c = currentChar();
                        if (c == '.')
                            octets++;
                        else if (c < '0' || c > '9') break;
                        lastIPaddress = lastIPaddress + c;
                    }
                    if (octets == 4) {
                        current = Tokens.IPV4;
                        return;
                    } else {
                        current = Tokens.ERROR;
                        return;
                    }
                }
                if (c < '0' || c > '9') return;
                lastNumber = lastNumber * 10 + c - '0';
            }
        }
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
                if (atEnd()) return;
                c = currentChar();
                if (!isExtendedNameChar(c)) return;
                lastCommand = lastCommand + c;
            }
        }
        if (c == '"') {
            StringBuilder str = new StringBuilder();
            current = Tokens.STRING;
            index++;
            while (true) {
                if (atEnd()) break;
                c = toParse.charAt(index);
                index++;
                if (c == '"') break;
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
                if (atEnd()) return;
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

    public Tokens current() {
        return current;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public int getLastNumber() {
        return lastNumber;
    }

    public String getLastString() {
        return lastString;
    }

    public String getLastIPAddress() {
        return lastIPaddress;
    }

    public String getLastConstant() {
        return lastConstant;
    }

    public static enum Tokens {
        BASIC_NAME,    // A or &A
        CONSTANT,      // DGRAM (an uppercase string)
        S_NAME,        // S1 or S13
        EXTENDED_NAME, // +CCGT or +C1M1 or +!%-_./:     The '+' will be configurable
        NUMBER,        // 123
        IPV4,          // 192.168.1.1
        SEMICOLON,     // ;
        COLON,         // :
        COMMA,         // ,
        QUESTION,      // ?
        EQUAL,         // =
        EQUAL_QUESTION,// =?
        SPACE,         // ' ' and '\r'
        STRING,        // "Some text"
        ERROR,
        EOF;
    }
}
