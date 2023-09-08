package banking_system4;
public class User {

    private String id;
    private String hashedPassword;
    private String salt;
    private String cardId;  // 添加 cardId 属性

    public User(String id, String password, String cardId) {
        this.id = id;
        setPassword(password);
        this.cardId = cardId;
    }

    public String getId() {
        return id;
    }

    public void setPassword(String password) {
        this.salt = HashUtil.generateSalt();
        this.hashedPassword = HashUtil.hashPassword(password, salt);
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getSalt() {
        return salt;
    }

    public String getCardId() {
        return cardId;
    }
}
