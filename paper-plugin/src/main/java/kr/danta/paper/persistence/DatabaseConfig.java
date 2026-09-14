package kr.danta.paper.persistence;

public record DatabaseConfig(
        boolean enabled,
        String host,
        int port,
        String database,
        String user,
        String password,
        int connectTimeoutSeconds
) {
    public DatabaseConfig {
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host is blank");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("port must be 1..65535");
        if (database == null || database.isBlank()) throw new IllegalArgumentException("database is blank");
        if (user == null || user.isBlank()) throw new IllegalArgumentException("user is blank");
        if (password == null) password = "";
        if (connectTimeoutSeconds < 1 || connectTimeoutSeconds > 60) {
            throw new IllegalArgumentException("connectTimeoutSeconds must be 1..60");
        }
    }

    public String jdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database
                + "?connectTimeout=" + connectTimeoutSeconds
                + "&socketTimeout=10"
                + "&ApplicationName=DantaServer";
    }

    public String safeDescription() {
        return host + ":" + port + "/" + database + " user=" + user;
    }
}
