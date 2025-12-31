package com.zubeyde.auto.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class InspectionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "inspection_id")
    private Inspection inspection;

    public InspectionDetail( Long id,boolean isPassed, ChecklistTemplate checkItem) {
        this.isPassed = isPassed;
        this.id = id;
        this.checkItem = checkItem;
    }

    public InspectionDetail() {
    }

    public InspectionDetail(Inspection inspection) {
        this.inspection=inspection;
    }

    public InspectionDetail(ChecklistTemplate checkItem) {
        this.checkItem = checkItem;

    }

    @ManyToOne
    @JoinColumn(name = "checklist_template_id")
    private ChecklistTemplate checkItem;

    private boolean isPassed; 
    private String inspectorNote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Inspection getInspection() { return inspection; }
    public void setInspection(Inspection inspection) { this.inspection = inspection; }

    public ChecklistTemplate getCheckItem() { return checkItem; }
    public void setCheckItem(ChecklistTemplate checkItem) { this.checkItem = checkItem; }

    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }

    public String getInspectorNote() { return inspectorNote; }
    public void setInspectorNote(String inspectorNote) { this.inspectorNote = inspectorNote; }
}