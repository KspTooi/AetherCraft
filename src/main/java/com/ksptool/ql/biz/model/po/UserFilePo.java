package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

@Entity
@Table(name = "USER_FILES")
@Data
public class UserFilePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("文件ID")
    private Long id;

    @Column(name = "user_id")
    @Comment("用户ID")
    private Long userId;

    @Column(name = "file_path", nullable = false, length = 500, unique = true)
    @Comment("存储的文件名")
    private String filepath;

    @Column(name = "original_file_name", nullable = false, length = 255)
    @Comment("原始文件名")
    private String originalFilename;

    @Column(name = "file_type", length = 100)
    @Comment("文件类型")
    private String fileType;

    @Column(name = "file_size")
    @Comment("文件大小(字节)")
    private Long fileSize;

    @Column(name = "sha256", length = 64)
    @Comment("文件SHA256哈希值")
    private String sha256;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @PrePersist
    public void prePersist() {
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
}