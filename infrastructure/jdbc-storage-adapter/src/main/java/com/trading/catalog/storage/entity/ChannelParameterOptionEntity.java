package com.trading.catalog.storage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("exchange_market_channel_param_allowed_values")
public class ChannelParameterOptionEntity {

    @Id
    private Long id;

    @Column("channel_param_id")
    private Long channelParamId;

    private String value;

    @Column("display_name")
    private String displayName;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("is_default")
    private boolean isDefault;

    private boolean enabled;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
