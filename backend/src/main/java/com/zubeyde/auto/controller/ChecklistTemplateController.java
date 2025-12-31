package com.zubeyde.auto.controller;

import com.zubeyde.auto.entity.ChecklistTemplate;
import com.zubeyde.auto.repository.ChecklistTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/checklist-templates")
public class ChecklistTemplateController {

    @Autowired
    private ChecklistTemplateRepository checklistTemplateRepository;

    @GetMapping
    public List<ChecklistTemplate> getAllChecklistTemplates() {
        return checklistTemplateRepository.findAll();
    }
}
