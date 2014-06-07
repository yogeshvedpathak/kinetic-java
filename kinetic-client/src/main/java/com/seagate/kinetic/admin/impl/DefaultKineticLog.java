/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.seagate.kinetic.admin.impl;

import java.util.ArrayList;
import java.util.List;

import kinetic.admin.Capacity;
import kinetic.admin.Configuration;
import kinetic.admin.Interface;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.admin.Limits;
import kinetic.admin.MessageType;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.KineticException;

import com.seagate.kinetic.proto.Kinetic.Message;

public class DefaultKineticLog implements KineticLog {
    private Message response = null;

    public DefaultKineticLog(Message response) {
        this.response = response;
    }

    @Override
    public List<Utilization> getUtilization() throws KineticException {
        validate(response);

        List<Utilization> utils = new ArrayList<Utilization>();
        List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Utilization> utilizations = response
                .getCommand().getBody().getGetLog().getUtilizationList();
        if (null == utilizations || utilizations.isEmpty()
                || 0 == utilizations.size()) {
            throw new KineticException(
                    "Response message error: utilization list is null or empty or size is 0.");
        }

        for (int i = 0; i < utilizations.size(); i++) {
            Utilization utilInfo = new Utilization();
            utilInfo.setName(utilizations.get(i).getName());
            utilInfo.setUtility(utilizations.get(i).getValue());

            utils.add(utilInfo);
        }

        return utils;
    }

    @Override
    public List<Temperature> getTemperature() throws KineticException {
        validate(response);

        List<Temperature> temps = new ArrayList<Temperature>();
        List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Temperature> Temperatures = response
                .getCommand().getBody().getGetLog().getTemperatureList();
        if (null == Temperatures || Temperatures.isEmpty()
                || 0 == Temperatures.size()) {
            throw new KineticException(
                    "Response message error: temperature list is null or empty or size is 0.");
        }

        for (int i = 0; i < Temperatures.size(); i++) {
            Temperature tempInfo = new Temperature();
            if (Temperatures.get(i).hasName()) {
                tempInfo.setName(Temperatures.get(i).getName());
            }

            if (Temperatures.get(i).hasMaximum()) {
                tempInfo.setMax(Temperatures.get(i).getMaximum());
            }

            if (Temperatures.get(i).hasMinimum()) {
                tempInfo.setMin(Temperatures.get(i).getMinimum());
            }

            if (Temperatures.get(i).hasTarget()) {
                tempInfo.setTarget(Temperatures.get(i).getTarget());
            }

            if (Temperatures.get(i).hasCurrent()) {
                tempInfo.setCurrent(Temperatures.get(i).getCurrent());
            }

            temps.add(tempInfo);
        }

        return temps;
    }

    @Override
    public Capacity getCapacity() throws KineticException {
        validate(response);

        com.seagate.kinetic.proto.Kinetic.Message.GetLog.Capacity capacity = response
                .getCommand().getBody().getGetLog().getCapacity();

        if (null == response.getCommand().getBody().getGetLog().getCapacity()) {
            throw new KineticException(
                    "Response message error: capacity is null.");
        }

        Capacity capacInfo = new Capacity();

        if (capacity.hasNominalCapacityInBytes()) {
            capacInfo.setTotal(capacity.getNominalCapacityInBytes());
        }

        if (capacity.hasPortionFull()) {
            capacInfo.setRemaining(capacity.getPortionFull());
        }

        return capacInfo;
    }

    private void validate(Message response) throws KineticException {
        if (null == response) {
            throw new KineticException(
                    "Response message error: response is null");
        }

        if (null == response.getCommand()) {
            throw new KineticException(
                    "Response message error: command is null");
        }

        if (null == response.getCommand().getBody()) {
            throw new KineticException("Response message error: body is null");
        }

        if (null == response.getCommand().getBody().getGetLog()) {
            throw new KineticException("Response message error: getlog is null");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws KineticException
     * 
     * @see KineticAdminClient#getLog(List)
     */
    @Override
    public KineticLogType[] getContainedLogTypes() throws KineticException {

        validate(response);

        List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type> typeOfList = response
                .getCommand().getBody().getGetLog().getTypeList();

        if (null == typeOfList || typeOfList.isEmpty()
                || 0 == typeOfList.size()) {
            return null;
        }

        int typeOfListSize = typeOfList.size();
        KineticLogType[] types = new KineticLogType[typeOfListSize];

        for (int i = 0; i < typeOfListSize; i++) {

            switch (typeOfList.get(i)) {

            case UTILIZATIONS:
                types[i] = KineticLogType.UTILIZATIONS;
                break;
            case TEMPERATURES:
                types[i] = KineticLogType.TEMPERATURES;
                break;
            case CAPACITIES:
                types[i] = KineticLogType.CAPACITIES;
                break;
            case CONFIGURATION:
                types[i] = KineticLogType.CONFIGURATION;
                break;
            case STATISTICS:
                types[i] = KineticLogType.STATISTICS;
                break;
            case MESSAGES:
                types[i] = KineticLogType.MESSAGES;
                break;
            case LIMITS:
                types[i] = KineticLogType.LIMITS;
                break;
            default:
                ;

            }
        }

        return types;
    }

    /**
     * {@inheritDoc}
     * 
     * @see #getContainedLogTypes()
     * @see KineticAdminClient#getLog(List)
     */
    @Override
    public byte[] getMessages() throws KineticException {

        validate(response);

        byte[] message = response.getCommand().getBody().getGetLog()
                .getMessages().toByteArray();

        if (null == response.getCommand().getBody().getGetLog().getMessages()) {
            throw new KineticException(
                    "Response message error: getlog message is null.");
        }

        return message;
    }

    @Override
    public Configuration getConfiguration() throws KineticException {

        validate(response);

        com.seagate.kinetic.proto.Kinetic.Message.GetLog.Configuration configuration = response
                .getCommand().getBody().getGetLog().getConfiguration();

        if (null == response.getCommand().getBody().getGetLog()
                .getConfiguration()) {
            throw new KineticException(
                    "Response message error: configuration is null.");
        }

        Configuration configurationInfo = new Configuration();

        if (configuration.hasCompilationDate()) {
            configurationInfo.setCompilationDate(configuration
                    .getCompilationDate());
        }

        List<Interface> interfaces = new ArrayList<Interface>();

        List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Configuration.Interface> interfacesFromResponse = configuration
                .getInterfaceList();
        if (null != interfacesFromResponse
                && (!interfacesFromResponse.isEmpty())
                && 0 != interfacesFromResponse.size()) {
            for (com.seagate.kinetic.proto.Kinetic.Message.GetLog.Configuration.Interface interfaceFromMessage : interfacesFromResponse) {
                Interface interfaceInfo = new Interface();

                if (interfaceFromMessage.hasIpv4Address()) {
                    interfaceInfo.setIpv4Address(interfaceFromMessage
                            .getIpv4Address().toStringUtf8());
                }

                if (interfaceFromMessage.hasIpv6Address()) {
                    interfaceInfo.setIpv6Address(interfaceFromMessage
                            .getIpv6Address().toStringUtf8());
                }

                if (interfaceFromMessage.hasMAC()) {
                    String macAddr = interfaceFromMessage.getMAC()
                            .toStringUtf8();
                    interfaceInfo.setMAC(macAddr);
                }

                if (interfaceFromMessage.hasName()) {
                    interfaceInfo.setName(interfaceFromMessage.getName());
                }

                interfaces.add(interfaceInfo);
            }
        }

        configurationInfo.setInterfaces(interfaces);

        if (configuration.hasModel()) {
            configurationInfo.setModel(configuration.getModel());
        }

        if (configuration.hasPort()) {
            configurationInfo.setPort(configuration.getPort());
        }

        if (configuration.hasSerialNumber()) {
            configurationInfo.setSerialNumber(configuration.getSerialNumber()
                    .toStringUtf8());
        }

        if (configuration.hasSourceHash()) {
            configurationInfo.setSourceHash(configuration.getSourceHash());
        }

        if (configuration.hasProtocolVersion()) {
            configurationInfo.setProtocolVersion(configuration
                    .getProtocolVersion());
        }

        if (configuration.hasProtocolCompilationDate()) {
            configurationInfo.setProtocolCompilationDate(configuration
                    .getProtocolCompilationDate());
        }

        if (configuration.hasProtocolSourceHash()) {
            configurationInfo.setProtocolSourceHash(configuration
                    .getProtocolSourceHash());
        }

        if (configuration.hasTlsPort()) {
            configurationInfo.setTlsPort(configuration.getTlsPort());
        }

        if (configuration.hasVendor()) {
            configurationInfo.setVendor(configuration.getVendorBytes()
                    .toStringUtf8());
        }

        if (configuration.hasVersion()) {
            configurationInfo.setVersion(configuration.getVersionBytes()
                    .toStringUtf8());
        }

        return configurationInfo;
    }

    @Override
    public List<Statistics> getStatistics() throws KineticException {
        validate(response);

        List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Statistics> statisticsOfMessageList = response
                .getCommand().getBody().getGetLog().getStatisticsList();

        if (null == response.getCommand().getBody().getGetLog()
                .getStatisticsList()) {
            throw new KineticException(
                    "Response message error: Statistic is null.");
        }

        List<Statistics> statisticOfList = new ArrayList<Statistics>();

        for (com.seagate.kinetic.proto.Kinetic.Message.GetLog.Statistics statistics : statisticsOfMessageList) {
            Statistics statisticsInfo = new Statistics();
            if (statistics.hasBytes()) {
                statisticsInfo.setBytes(statistics.getBytes());
            }

            if (statistics.hasCount()) {
                statisticsInfo.setCount(statistics.getCount());
            }

            switch (statistics.getMessageType()) {
            case GET:
                statisticsInfo.setMessageType(MessageType.GET);
                break;
            case PUT:
                statisticsInfo.setMessageType(MessageType.PUT);
                break;
            case DELETE:
                statisticsInfo.setMessageType(MessageType.DELETE);
                break;
            case GETNEXT:
                statisticsInfo.setMessageType(MessageType.GETNEXT);
                break;
            case GETPREVIOUS:
                statisticsInfo.setMessageType(MessageType.GETPREVIOUS);
                break;
            case GETKEYRANGE:
                statisticsInfo.setMessageType(MessageType.GETKEYRANGE);
                break;
            case GETVERSION:
                statisticsInfo.setMessageType(MessageType.GETVERSION);
                break;
            case SETUP:
                statisticsInfo.setMessageType(MessageType.SETUP);
                break;
            case GETLOG:
                statisticsInfo.setMessageType(MessageType.GETLOG);
                break;
            case SECURITY:
                statisticsInfo.setMessageType(MessageType.SECURITY);
                break;
            case PEER2PEERPUSH:
                statisticsInfo.setMessageType(MessageType.PEER2PEERPUSH);
                break;
            default:
                ;
            }

            statisticOfList.add(statisticsInfo);
        }

        return statisticOfList;
    }

    @Override
    public Limits getLimits() throws KineticException {
        validate(response);

        com.seagate.kinetic.proto.Kinetic.Message.GetLog.Limits limits = response
                .getCommand().getBody().getGetLog().getLimits();

        if (null == response.getCommand().getBody().getGetLog().getLimits()) {
            throw new KineticException(
                    "Response message error: limits is null.");
        }

        Limits LimitsInfo = new Limits();

        if (limits.hasMaxKeySize()) {
            LimitsInfo.setMaxKeySize(limits.getMaxKeySize());
        }

        if (limits.hasMaxValueSize()) {
            LimitsInfo.setMaxValueSize(limits.getMaxValueSize());
        }

        if (limits.hasMaxVersionSize()) {
            LimitsInfo.setMaxVersionSize(limits.getMaxVersionSize());
        }

        if (limits.hasMaxTagSize()) {
            LimitsInfo.setMaxTagSize(limits.getMaxTagSize());
        }

        if (limits.hasMaxConnections()) {
            LimitsInfo.setMaxConnections(limits.getMaxConnections());
        }

        if (limits.hasMaxOutstandingReadRequests()) {
            LimitsInfo.setMaxOutstandingReadRequests(limits
                    .getMaxOutstandingReadRequests());
        }

        if (limits.hasMaxOutstandingWriteRequests()) {
            LimitsInfo.setMaxOutstandingWriteRequests(limits
                    .getMaxOutstandingWriteRequests());
        }

        if (limits.hasMaxMessageSize()) {
            LimitsInfo.setMaxMessageSize(limits.getMaxMessageSize());
        }

        if (limits.hasMaxKeyRangeCount()) {
            LimitsInfo.setMaxKeyRangeCount(limits.getMaxKeyRangeCount());
        }

        return LimitsInfo;
    }
}