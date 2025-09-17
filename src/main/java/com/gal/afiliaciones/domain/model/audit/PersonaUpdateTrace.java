package com.gal.afiliaciones.domain.model.audit;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime; // timestamptz -> OffsetDateTime (o Instant)

@Entity
@Table(name = "persona_update_trace")
public class PersonaUpdateTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_doc")
    private String actorDoc;

    @CreationTimestamp
    @Column(name = "ts", nullable = false, updatable = false)
    private OffsetDateTime ts;

    @Column(name = "actor_role_id")
    private Long actorRoleId;

    @Column(name = "actor_role_name")
    private String actorRoleName;

    @Column(name = "target_user_doc")
    private String targetUserDoc;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public String getActorDoc() { return actorDoc; }
    public void setActorDoc(String actorDoc) { this.actorDoc = actorDoc; }

    public OffsetDateTime getTs() { return ts; }
    public void setTs(OffsetDateTime ts) { this.ts = ts; }

    public Long getActorRoleId() { return actorRoleId; }
    public void setActorRoleId(Long actorRoleId) { this.actorRoleId = actorRoleId; }

    public String getActorRoleName() { return actorRoleName; }
    public void setActorRoleName(String actorRoleName) { this.actorRoleName = actorRoleName; }

    public String getTargetUserDoc() { return targetUserDoc; }
    public void setTargetUserDoc(String targetUserDoc) { this.targetUserDoc = targetUserDoc; }
}
