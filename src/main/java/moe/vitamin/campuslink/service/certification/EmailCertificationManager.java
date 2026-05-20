package moe.vitamin.campuslink.service.certification;

import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;

public class EmailCertificationManager {

    public static EmailCertificationManager init() {
        EmailCertificationDao.init();

        return new EmailCertificationManager();
    }

    private EmailCertificationManager() {

    }


}
