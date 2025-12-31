package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.entity.Payment;
import com.zubeyde.auto.repository.PaymentRepository;
import com.zubeyde.auto.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class NotificationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAppointmentNotification() {
        String result = notificationService.AppointmentNotification();
        Assertions.assertEquals("Randevunuz başarı ile oluşturuldu", result);
    }

    @Test
    public void testIncspectionCompletedNotification_Passed() {
        Inspection inspection = new Inspection();
        inspection.setResult("GEÇTİ");

        String result = notificationService.IncspectionCompletedNotification(inspection);
        Assertions.assertEquals("İnclemeniz başarı ile tamamlandı : Aracınız GEÇTİ", result);
    }
    @Test
    public void testInspectionCompletedNotification_Passed_Not(){
        Inspection inspection=new Inspection();
        inspection.setResult("KALDI");
        InspectionDetail detail=new InspectionDetail(1L,true,null);
        inspection.setDetails(List.of(detail));
        String result=notificationService.IncspectionCompletedNotification(inspection);
        Assertions.assertTrue(result.contains("İnceleme sonucunuz negatif , Lütfen belirtilen ağır hasarları gideriniz. "));
    }

    @Test
    public void testIncspectionCompletedNotification_Failed() {
        Inspection inspection = new Inspection();
        inspection.setResult("KALDI");

        List<InspectionDetail> details = new ArrayList<>();
        InspectionDetail detail1 = new InspectionDetail();
        detail1.setPassed(false);
        // Assuming toString or some property is used in the output, but the service uses toString() of the list.
        // Let's just check if it contains the failure message.
        details.add(detail1);
        inspection.setDetails(details);

        String result = notificationService.IncspectionCompletedNotification(inspection);
        Assertions.assertTrue(result.contains("İnceleme sonucunuz negatif"));
        Assertions.assertTrue(result.contains("Lütfen belirtilen ağır hasarları gideriniz"));
    }

    @Test
    public void testPaymentCompletedNotification_Paid() {
        Inspection inspection = new Inspection();
        inspection.setId(1L);

        Payment payment = new Payment();
        payment.setPaymentMethod("Credit Card");

        when(paymentRepository.findByInspectionId(1L)).thenReturn(payment);

        String result = notificationService.PaymentCompletedNotification(inspection);
        Assertions.assertEquals("Ödemeniz başarı ile alındı. ", result);
    }

    @Test
    public void testPaymentCompletedNotification_NotPaid() {
        Inspection inspection = new Inspection();
        inspection.setId(1L);

        Payment payment = new Payment();
        payment.setPaymentMethod(null);

        when(paymentRepository.findByInspectionId(1L)).thenReturn(payment);

        String result = notificationService.PaymentCompletedNotification(inspection);
        Assertions.assertEquals("Henüz ödeme yapmadınız. ", result);
    }
}

