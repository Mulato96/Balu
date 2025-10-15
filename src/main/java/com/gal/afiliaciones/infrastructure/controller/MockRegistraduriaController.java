package com.gal.afiliaciones.infrastructure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/mock/registraduria")
public class MockRegistraduriaController {

    @PostMapping(produces = MediaType.TEXT_XML_VALUE)
    public String mockRegistraduriaResponse(@RequestBody String request) {
        log.info("Mock registraduria service called with request: {}", request);

        String documentNumber = extractDocumentNumber(request);

        return generateMockResponse(documentNumber);
    }

    private String extractDocumentNumber(String request) {
        if (request.contains("<nuip>")) {
            int start = request.indexOf("<nuip>") + 6;
            int end = request.indexOf("</nuip>");
            if (start > 5 && end > start) {
                return request.substring(start, end);
            }
        }
        return "1000381834";
    }

    private String generateMockResponse(String documentNumber) {
        // Simulate different responses based on document number
        if ("11112111".equals(documentNumber)) {
            // Person not found response
            return String.format("""
                    <?xml version="1.0" ?>
                    <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
                        <S:Body>
                            <ns2:consultarCedulasResponse xmlns:ns2="http://service.positiva.gov.co/" xmlns:ns3="http://ws.ani.rnec.gov.co/">
                                <return>
                                    <return>
                                        <estadoConsulta>
                                            <numeroControl>2776747212</numeroControl>
                                            <codError>0</codError>
                                            <descripcionError>OK</descripcionError>
                                            <fechaHoraConsulta>2025-08-01 10:40:56</fechaHoraConsulta>
                                        </estadoConsulta>
                                        <datosCedulas>
                                            <datos>
                                                <nuip>%s</nuip>
                                                <codError>1</codError>
                                            </datos>
                                        </datosCedulas>
                                    </return>
                                </return>
                            </ns2:consultarCedulasResponse>
                        </S:Body>
                    </S:Envelope>
                    """, documentNumber);
        } else {
            // Person found response
            return String.format("""
                    <?xml version="1.0" ?>
                    <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
                        <S:Body>
                            <ns2:consultarCedulasResponse xmlns:ns2="http://service.positiva.gov.co/" xmlns:ns3="http://ws.ani.rnec.gov.co/">
                                <return>
                                    <return>
                                        <estadoConsulta>
                                            <numeroControl>2776727232</numeroControl>
                                            <codError>0</codError>
                                            <descripcionError>OK</descripcionError>
                                            <fechaHoraConsulta>2025-08-01 10:40:01</fechaHoraConsulta>
                                        </estadoConsulta>
                                        <datosCedulas>
                                            <datos>
                                                <nuip>%s</nuip>
                                                <codError>0</codError>
                                                <primerApellido>ROJAS</primerApellido>
                                                <particula> </particula>
                                                <segundoApellido>ASENCIO</segundoApellido>
                                                <primerNombre>CRISTIAN</primerNombre>
                                                <segundoNombre>CAMILO</segundoNombre>
                                                <municipioExpedicion>NEIVA</municipioExpedicion>
                                                <departamentoExpedicion>HUILA</departamentoExpedicion>
                                                <fechaExpedicion>10/03/2021</fechaExpedicion>
                                                <estadoCedula>0</estadoCedula>
                                                <numResolucion></numResolucion>
                                                <anoResolucion></anoResolucion>
                                                <genero>MASCULINO</genero>
                                                <fechaNacimiento>09/03/2003</fechaNacimiento>
                                                <informante></informante>
                                                <serial></serial>
                                                <fechaDefuncion></fechaDefuncion>
                                                <fechaReferencia></fechaReferencia>
                                                <fechaAfectacion></fechaAfectacion>
                                            </datos>
                                        </datosCedulas>
                                    </return>
                                </return>
                            </ns2:consultarCedulasResponse>
                        </S:Body>
                    </S:Envelope>
                    """, documentNumber);
        }
    }
} 