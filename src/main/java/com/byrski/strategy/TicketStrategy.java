package com.byrski.strategy;

import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.enums.TicketType;

import java.util.List;

/**
 * 票据策略接口，定义了处理不同类型票据的基本操作。
 * <p>
 * 该接口使用泛型来确保类型安全，所有实现类必须指定具体的票据实体类型。
 * </p>
 *
 * @param <T> 票据实体类型，必须继承自 BaseTicketEntity
 */
public interface TicketStrategy<T extends BaseTicketEntity> {

    /**
     * 处理票据的核心方法。
     * <p>
     * 实现类需要定义具体的票据处理逻辑。
     * </p>
     *
     * @param ticket 要处理的票据实体
     */
    void processTicket(T ticket);

    /**
     * 验证票据的有效性。
     * <p>
     * 实现类需要定义具体的验证逻辑，以确保票据符合业务规则。
     * </p>
     *
     * @param ticket 要验证的票据实体
     */
    void validateTicket(T ticket);

    /**
     * 获取策略对应的票据类型。
     * <p>
     * 每个策略实现类必须返回其支持的票据类型。
     * </p>
     *
     * @return 票据类型
     */
    TicketType getTicketType();

    /**
     * 保存票据实体。
     * <p>
     * 实现类需要定义具体的保存逻辑，通常涉及持久化操作。
     * </p>
     *
     * @param ticket 要保存的票据实体
     * @return 保存后的票据实体
     */
    T saveTicket(T ticket);

    /**
     * 根据活动ID获取该类型的所有票据列表
     * @param activityId 活动ID
     * @return 票据列表
     */
    List<T> getTicketsByActivityId(Long activityId);

    void doLog(T ticket);

    T getTicketById(Long ticketId);
}