package com.rnexchange.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BrokerBaselineDTO implements Serializable {

    private Long id;
    private String code;
    private String name;
    private String status;
    private String exchangeCode;
    private String exchangeName;
    private String exchangeTimezone;
    private String brokerAdminLogin;
    private List<String> exchangeMemberships = new ArrayList<>();
    private List<BrokerInstrumentDTO> instrumentCatalog = new ArrayList<>();
    private int instrumentCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeTimezone() {
        return exchangeTimezone;
    }

    public void setExchangeTimezone(String exchangeTimezone) {
        this.exchangeTimezone = exchangeTimezone;
    }

    public String getBrokerAdminLogin() {
        return brokerAdminLogin;
    }

    public void setBrokerAdminLogin(String brokerAdminLogin) {
        this.brokerAdminLogin = brokerAdminLogin;
    }

    public List<String> getExchangeMemberships() {
        return exchangeMemberships;
    }

    public void setExchangeMemberships(List<String> exchangeMemberships) {
        this.exchangeMemberships = exchangeMemberships;
    }

    public List<BrokerInstrumentDTO> getInstrumentCatalog() {
        return instrumentCatalog;
    }

    public void setInstrumentCatalog(List<BrokerInstrumentDTO> instrumentCatalog) {
        this.instrumentCatalog = instrumentCatalog;
    }

    public int getInstrumentCount() {
        return instrumentCount;
    }

    public void setInstrumentCount(int instrumentCount) {
        this.instrumentCount = instrumentCount;
    }
}
