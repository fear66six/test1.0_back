package com.byrski.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;
    @Value("${spring.security.jwt.token-expire-day}")
    int tokenExpireDay;
    @Value("${spring.security.jwt.trade-expire-second}")
    int tradeExpireSecond;

    @Resource
    private RedisUtils redisUtils;

    /**
     * 通过调用 deleteToken 使一个 Jwt 令牌失效。
     * 首先将传入的 headerToken 进行转换，若转换结果为 null 则返回 false。
     * 然后使用指定算法创建 JWTVerifier 进行令牌验证，若验证通过，
     * 获取令牌的 id 并调用 deleteToken 方法删除令牌，
     * 若验证异常则返回 false。
     * @param headerToken 要失效的 token
     * @return 执行结果，true 表示成功使令牌失效，false 表示失败
     */
    public boolean invalidateJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 删除 token。
     * 首先检查令牌是否为无效令牌，若是则抛出异常。
     * 计算当前时间与令牌过期时间的差值，将令牌加入 Redis 维护的黑名单，
     * 并设置过期时间。
     * @param uuid 令牌的 uuid
     * @param expireTime 令牌的到期时间
     * @return 执行结果，true 表示删除成功
     */
    private boolean deleteToken(String uuid, Date expireTime) {
        if (this.isInvalidToken(uuid))
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(), "该token已失效");
        Date now = new Date();
        // 计算令牌的过期时间
        long expire = Math.max(expireTime.getTime() - now.getTime(), 0);
        // 将令牌加入Redis维护的黑名单
        redisUtils.set(Const.JWT_BLACK_LIST + uuid, "", expire / 1000 + 1);
        return true;
    }

    /**
     * 判断 token 是否在黑名单中。
     * 通过 RedisUtils 的 exist 方法检查令牌是否存在于黑名单中。
     * @param uuid 令牌的 uuid
     * @return 是否在黑名单中，true 表示在黑名单中，false 表示不在
     */
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(redisUtils.exist(Const.JWT_BLACK_LIST + uuid));
    }

    /**
     * 创建用户登录的 Jwt 令牌。
     * 使用指定算法创建 JWT 令牌，设置令牌的唯一标识、用户 id、用户名、用户权限、
     * 过期时间和签发时间，并对令牌进行签名。
     * @param details 用户信息
     * @param id  用户 id
     * @param username 用户名
     * @return JWT 令牌
     */
    public String createUserJwt(UserDetails details, long id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTokenTime();
        // 每个令牌携带一个随机的uuid，以便后续进行令牌的拉黑处理
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * 创建管理员登录的 Jwt 令牌。
     * @param details 用户信息
     * @param id 管理员 id
     * @param username 管理员用户名
     * @return JWT 令牌
     */
    public String createAdminJwt(UserDetails details, long id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTokenTime();
        // 每个令牌携带一个随机的uuid，以便后续进行令牌的拉黑处理
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("admin_id", id)
                .withClaim("admin_name", username)
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * 创建 tradeId 的 Jwt 令牌。
     * 与 createUserJwt 类似，创建 JWT 令牌并设置相关信息，
     * 包括 tradeId、过期时间和签发时间等。
     * @param tradeId 要生成的 tradeId
     * @return JWT 令牌
     */
    public String createTradeIdJwt(Long tradeId) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTradeTime();
        // 每个令牌携带一个随机的uuid，以便后续进行令牌的拉黑处理
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("tradeId", tradeId)
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * 计算 token 过期时间。
     * 使用 Calendar 类将当前时间加上 tokenExpireDay 天的时间，得到过期时间。
     * @return 过期时间
     */
    public Date expireTokenTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, tokenExpireDay * 24);
        return calendar.getTime();
    }

    /**
     * 计算 trade 令牌的过期时间。
     * 使用 Calendar 类将当前时间加上 tradeExpireSecond 秒的时间，得到过期时间。
     * @return 过期时间
     */
    public Date expireTradeTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, tradeExpireSecond);
        return calendar.getTime();
    }

    /**
     * 解析 Jwt。
     * 首先将传入的 headerToken 进行转换，若转换结果为 null 则抛出异常。
     * 然后使用指定算法创建 JWTVerifier 进行令牌验证，
     * 检查令牌是否在黑名单中，若在则抛出异常，
     * 最后检查令牌是否过期，未过期则返回解析出的 Jwt 令牌，过期则返回 null。
     * @param headerToken 要解析的 token
     * @return 解析的 Jwt token，若过期则为 null
     */
    public DecodedJWT resolveUserJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null)
            return null;
//            throw new ByrSkiException(ReturnCode.UNAUTHORIZED);
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        // 检验token有没有异常
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            // 判断令牌是否在黑名单中
            if (this.isInvalidToken(verify.getId()))
                return null;
//                throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(), "令牌在黑名单中");
            // 判断令牌是否过期，未过期则返回解析出的Jwt令牌
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(),  "令牌校验失败，" + e.getMessage());
        }
    }

    public DecodedJWT resolveAdminJwt(String adminToken) {
        String token = this.convertToken(adminToken);
        if (token == null)
            return null;
//            throw new ByrSkiException(ReturnCode.UNAUTHORIZED);
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        // 检验token有没有异常
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            // 判断令牌是否在黑名单中
            if (this.isInvalidToken(verify.getId()))
                return null;
//                throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(), "令牌在黑名单中");
            // 判断令牌是否过期，未过期则返回解析出的Jwt令牌
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(),  "令牌校验失败，" + e.getMessage());
        } }

    /**
     * 解析 Trade Jwt。
     * 与 resolveUserJwt 类似，进行令牌验证、黑名单检查和过期检查，
     * 并处理异常情况。
     * @param tradeToken 要解析的 trade 令牌
     * @return 解析的 trade Jwt token，若过期则为 null
     */
    public DecodedJWT resolveTradeJwt(String tradeToken) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        // 检验token有没有异常
        try {
            DecodedJWT verify = jwtVerifier.verify(tradeToken);
            // 判断令牌是否在黑名单中
            if (this.isInvalidToken(verify.getId()))
                throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(), "令牌在黑名单中");
            // 判断令牌是否过期，未过期则返回解析出的Jwt令牌
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(),  "令牌校验失败，" + e.getMessage());
        }
    }

    /**
     * 检查 token 有效性。
     * 首先判断传入的 headerToken 是否为 null 或者不以 "Bearer " 开头，
     * 若是，则认为该 token 无效，返回 null；
     * 否则，将 "Bearer " 前缀去掉，返回剩余部分作为有效的 token。
     * @param headerToken Header 中携带的 Token
     * @return 返回切割好的 token
     */
    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    /**
     * 解析 jwt 中的 userDetails。
     * 从传入的 jwt 令牌信息中提取相关信息，包括用户名、密码和用户权限，
     * 并使用这些信息构建一个 UserDetails 对象。
     * 这里密码使用 "******" 作为占位符，可能需要根据实际情况修改。
     * @param jwt jwt 令牌信息
     * @return userDetails 信息
     */
    public UserDetails toUserDetails(DecodedJWT jwt) {
        Map<String, Claim> claimMap = jwt.getClaims();
        return User
                .withUsername(claimMap.get("name").asString())
                .password("******")
                .authorities(claimMap.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 从 jwt 中获取用户 id。
     * 从传入的 jwt 令牌信息中提取用户 id 信息。
     * 首先获取 jwt 的 claim 映射，然后从中提取 id 信息并转换为 Long 类型。
     * @param jwt jwt 令牌信息
     * @return 用户 id
     */
    public Long toId(DecodedJWT jwt) {
        Map<String, Claim> claimMap = jwt.getClaims();
        return claimMap.get("id").asLong();
    }

    /**
     * 解析 jwt 中的 userDetails。
     * 从传入的 jwt 令牌信息中提取相关信息，包括用户名、密码和用户权限，
     * 并使用这些信息构建一个 UserDetails 对象。
     * 这里密码使用 "******" 作为占位符，可能需要根据实际情况修改。
     * @param jwt jwt 令牌信息
     * @return userDetails 信息
     */
    public UserDetails toAdminDetails(DecodedJWT jwt) {
        Map<String, Claim> claimMap = jwt.getClaims();
        return User
                .withUsername(claimMap.get("admin_name").asString())
                .password("******")
                .authorities(claimMap.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 从 jwt 中获取用户 id。
     * 从传入的 jwt 令牌信息中提取用户 id 信息。
     * 首先获取 jwt 的 claim 映射，然后从中提取 id 信息并转换为 Long 类型。
     * @param jwt jwt 令牌信息
     * @return 用户 id
     */
    public Long toAdminId(DecodedJWT jwt) {
        Map<String, Claim> claimMap = jwt.getClaims();
        return claimMap.get("admin_id").asLong();
    }

    /**
     * 从 jwt 中获取订单 id。
     * 首先从 jwt 的 claim 映射中提取订单 id 信息并转换为 Long 类型。
     * 然后调用 deleteToken 方法删除该 jwt 对应的 token，
     * 若删除成功则返回订单 id，否则抛出异常。
     * @param jwt jwt 令牌信息
     * @return 订单 id
     */
    public Long toTradeId(DecodedJWT jwt) {
        Map<String, Claim> claimMap = jwt.getClaims();
        Long tradeId = claimMap.get("tradeId").asLong();
        if (deleteToken(jwt.getId(), jwt.getExpiresAt()))
            return tradeId;
        throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(), "订单二维码解密失败");
    }
}
