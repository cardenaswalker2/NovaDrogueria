package com.example.demo.service;

import com.example.demo.model.AppConfig;
import com.example.demo.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

    @Autowired
    private AppConfigRepository appConfigRepository;

    private static final String SINGLETON_ID = "singleton";

    public AppConfig getAppConfig() {
        return appConfigRepository.findById(SINGLETON_ID).orElseGet(() -> {
            AppConfig defaultConfig = new AppConfig();
            defaultConfig.setId(SINGLETON_ID);
            defaultConfig.setStoreName("Nova Droguería");
            defaultConfig.setDescription("Tu farmacia de confianza con tecnología y cercanía.");
            defaultConfig.setAddress("Calle 10 # 5-20, Centro Histórico");
            defaultConfig.setPhone("+57 300 123 4567");
            defaultConfig.setWhatsappNumber("573001234567");
            defaultConfig.setSchedule("Lunes a Sábado: 8:00 AM - 9:00 PM | Domingos y Festivos: 9:00 AM - 6:00 PM");
            defaultConfig.setWelcomeMessage("¡Hola! ¿En qué podemos ayudarte con tu salud hoy?");
            return appConfigRepository.save(defaultConfig);
        });
    }

    public AppConfig updateAppConfig(AppConfig newConfig) {
        AppConfig config = getAppConfig();
        config.setStoreName(newConfig.getStoreName());
        config.setDescription(newConfig.getDescription());
        config.setAddress(newConfig.getAddress());
        config.setPhone(newConfig.getPhone());
        config.setWhatsappNumber(newConfig.getWhatsappNumber());
        config.setSchedule(newConfig.getSchedule());
        config.setWelcomeMessage(newConfig.getWelcomeMessage());
        return appConfigRepository.save(config);
    }
}
