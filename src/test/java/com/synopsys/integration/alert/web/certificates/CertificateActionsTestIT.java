package com.synopsys.integration.alert.web.certificates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.web.model.CertificateModel;

@Transactional
public class CertificateActionsTestIT extends BaseCertificateTestIT {

    @Test
    public void readAllEmptyListTest() {
        assertTrue(certificateActions.readCertificates().isEmpty());
    }

    @Test
    public void createCertificateTest() throws Exception {
        String certificateContent = readCertificateContents();
        CertificateModel savedCertificate = createCertificate();

        assertNotNull(savedCertificate.getId());
        assertEquals(TEST_ALIAS, savedCertificate.getAlias());
        assertEquals(certificateContent, savedCertificate.getCertificateContent());
    }

    @Test
    public void createCertificateIdTest() throws Exception {
        String certificateContent = readCertificateContents();
        try {
            CertificateModel certificate = new CertificateModel("alias", certificateContent);
            certificate.setId("badId");
            certificateActions.createCertificate(certificate);
            fail();
        } catch (AlertDatabaseConstraintException ex) {
            assertTrue(ex.getMessage().contains("id cannot be present"));
        }
    }

    @Test
    public void readAllTest() throws Exception {
        createCertificate();
        assertEquals(1, certificateActions.readCertificates().size());
    }

    @Test
    public void readSingleCertificateTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();
        Optional<CertificateModel> actualCertificate = certificateActions.readCertificate(Long.valueOf(expectedCertificate.getId()));
        assertTrue(actualCertificate.isPresent());
        assertEquals(expectedCertificate, actualCertificate.get());
    }

    @Test
    public void updateCertificateTest() throws Exception {
        String certificateContent = readCertificateContents();
        CertificateModel savedCertificate = createCertificate();

        String updatedAlias = "updated-alias";
        CertificateModel newModel = new CertificateModel(savedCertificate.getId(), updatedAlias, certificateContent);
        Optional<CertificateModel> updatedCertificate = certificateActions.updateCertificate(Long.valueOf(savedCertificate.getId()), newModel);
        assertTrue(updatedCertificate.isPresent());

        CertificateModel updatedModel = updatedCertificate.get();
        assertEquals(savedCertificate.getId(), updatedModel.getId());
        assertEquals(updatedAlias, updatedModel.getAlias());
        assertEquals(certificateContent, updatedModel.getCertificateContent());
    }

    @Test
    public void updateCertificateMissingIdTest() throws Exception {
        String certificateContent = readCertificateContents();
        CertificateModel certificate = new CertificateModel("-1", certificateContent);
        Optional<CertificateModel> result = certificateActions.updateCertificate(-1L, certificate);
        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteCertificateTest() throws Exception {
        CertificateModel savedCertificate = createCertificate();
        certificateActions.deleteCertificate(Long.valueOf(savedCertificate.getId()));
        assertTrue(customCertificateRepository.findAll().isEmpty());
    }
}
