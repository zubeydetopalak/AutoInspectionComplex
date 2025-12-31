package com.zubeyde.auto.service;

import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
public class NotificationService {
    @Autowired
    private PaymentRepository repository;
    public String AppointmentNotification(){
        return "Randevunuz başarı ile oluşturuldu";
    }
    public String IncspectionCompletedNotification(Inspection inspection){
        List<InspectionDetail> liste=new ArrayList<>();
        if (inspection.getResult() != null && inspection.getResult().equals("KALDI")){
            if (inspection.getDetails() != null) {
                for (InspectionDetail inspectionDetail : inspection.getDetails()) {
                    if(!inspectionDetail.isPassed()){
                        liste.add(inspectionDetail);
                    }
                }
            }
            String message= "İnceleme sonucunuz negatif , Lütfen belirtilen ağır hasarları gideriniz. "+liste.toString();
            return message;

        }
        return "İnclemeniz başarı ile tamamlandı : Aracınız GEÇTİ";
    }
    public String PaymentCompletedNotification(Inspection inspection){
        if(repository.findByInspectionId(inspection.getId()).getPaymentMethod()!=null){
            return "Ödemeniz başarı ile alındı. ";
        }
        return "Henüz ödeme yapmadınız. ";
    }
}
