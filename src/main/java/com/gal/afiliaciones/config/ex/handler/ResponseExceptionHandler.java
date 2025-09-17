package com.gal.afiliaciones.config.ex.handler;

import com.gal.afiliaciones.config.ex.*;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.sat.SatUpstreamError;
import com.gal.afiliaciones.config.ex.sat.SatError;
import com.gal.afiliaciones.config.ex.addoption.ActivityMaxSizeException;
import com.gal.afiliaciones.config.ex.affiliation.*;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.cancelaffiliation.CancelAffiliationNotFoundException;
import com.gal.afiliaciones.config.ex.cancelaffiliation.DateCancelAffiliationException;
import com.gal.afiliaciones.config.ex.card.ErrorGeneratedCard;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.economicactivity.CodeAndDescriptionException;
import com.gal.afiliaciones.config.ex.economicactivity.CodeCIIUNotFound;
import com.gal.afiliaciones.config.ex.economicactivity.CodeCIIUShorterLength;
import com.gal.afiliaciones.config.ex.economicactivity.DescriptionNotFound;
import com.gal.afiliaciones.config.ex.generalnovelty.GeneralNoveltyException;
import com.gal.afiliaciones.config.ex.otp.OtpCodeExpired;
import com.gal.afiliaciones.config.ex.otp.OtpCodeInvalid;
import com.gal.afiliaciones.config.ex.typeemployerdocumentrequested.TypeEmployerDocumentRequested;
import com.gal.afiliaciones.config.ex.validationpreregister.*;
import com.gal.afiliaciones.config.ex.workerretirement.WorkerRetirementException;
import com.gal.afiliaciones.config.ex.workspaceofficial.WorkspaceOptionOfficialException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementNotFoundException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementValidationException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementConflictException;
import com.gal.afiliaciones.infrastructure.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String LOG_ERROR_CREATE_USER = "Create User: {}";
    private static final String LOG_ERROR_CODE_AND_DESCRIPTION_NULL = "Code and description is null: {}";

    @ExceptionHandler(value = {BusException.class})
    public ResponseEntity<ErrorResponse> handlerNoFoundInRegistryEx(BusException exception) {
        log.error("User not found in registry: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {AffiliateNotFound.class})
    public ResponseEntity<ErrorResponse> handlerAffiliationNotFoundEx(AffiliateNotFound exception) {
        log.error("affiliation not found: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {IndependentRelationShipException.class})
    public ResponseEntity<ErrorResponse> handlerIndependentRelationShipEx(IndependentRelationShipException exception) {
        log.error("independent relationship validation: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {InactiveStatusError.class})
    public ResponseEntity<ErrorResponse> handlerUserInactiveStatusEx(InactiveStatusError exception) {
        log.error("User is inactive: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UpdateNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUpdateNotFoundEx(UpdateNotFoundException exception) {
        log.error("updates not exist by affiliation: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {DocumentsFromCollectionNotFoundExcepcion.class})
    public ResponseEntity<ErrorResponse> handleDocumentsNotFoundEx(DocumentsFromCollectionNotFoundExcepcion exception) {
        log.error("docuemnts not exist from collection: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {DisplacementNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleWorkerDisplacementNotFound(DisplacementNotFoundException exception) {
        log.error("Worker displacement not found: {}", exception.getMessage());
        return new ResponseEntity<>(new ErrorResponse(Error.Type.REGISTER_NOT_FOUND, exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {java.lang.reflect.InvocationTargetException.class})
    public ResponseEntity<ErrorResponse> handleInvocationTarget(java.lang.reflect.InvocationTargetException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof DisplacementNotFoundException dnfe) {
            log.error("Worker displacement not found (wrapped): {}", dnfe.getMessage());
            return new ResponseEntity<>(new ErrorResponse(Error.Type.REGISTER_NOT_FOUND, dnfe.getMessage()), HttpStatus.NOT_FOUND);
        }
        if (cause instanceof DisplacementConflictException dce) {
            log.error("Worker displacement conflict (wrapped): {}", dce.getMessage());
            return new ResponseEntity<>(new ErrorResponse(Error.Type.ERROR_AFFILIATION_ALREADY_EXISTS, dce.getMessage()), HttpStatus.CONFLICT);
        }
        if (cause instanceof DisplacementValidationException dve) {
            log.error("Worker displacement validation (wrapped): {}", dve.getMessage());
            return new ResponseEntity<>(new ErrorResponse(Error.Type.INVALID_ARGUMENT, dve.getMessage()), HttpStatus.BAD_REQUEST);
        }
        log.error("InvocationTargetException: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(Error.Type.UNKNOWN_ERROR, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {DisplacementValidationException.class})
    public ResponseEntity<ErrorResponse> handleDisplacementValidation(DisplacementValidationException ex) {
        log.error("Worker displacement validation: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(Error.Type.INVALID_ARGUMENT, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {DisplacementConflictException.class})
    public ResponseEntity<ErrorResponse> handleDisplacementConflict(DisplacementConflictException ex) {
        log.error("Worker displacement conflict: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(Error.Type.ERROR_AFFILIATION_ALREADY_EXISTS, ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {PendingAffiliationError.class})
    public ResponseEntity<ErrorResponse> handlerUserPendingAffiliationEx(PendingAffiliationError exception) {
        log.error("User inactive by pending affiliation: {}", exception.getError().getMessage());

        List<String> otpData = new ArrayList<>();
        otpData.add(exception.getOtpRequestDTO().getCedula());
        otpData.add(exception.getOtpRequestDTO().getDestinatario());
        otpData.add(exception.getOtpRequestDTO().getNombre());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage(), otpData),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {OtpCodeInvalid.class})
    public ResponseEntity<ErrorResponse> handlerNoFoundInRegistryEx(OtpCodeInvalid exception) {
        log.error("Code otp invalid: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {OtpCodeExpired.class})
    public ResponseEntity<ErrorResponse> handlerNoFoundInRegistryEx(OtpCodeExpired exception) {
        log.error("Code otp expired: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UserNotRegisteredException.class})
    public ResponseEntity<ErrorResponse> userNotFoundInKeycloakEx(UserNotRegisteredException ex) {
        log.error("User not found in the system: {}", ex.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getError().getType(), ex.getError().getMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(value = {EmailAlreadyExists.class})
    public ResponseEntity<ErrorResponse> handlerEmailAlreadyExistsEx(EmailAlreadyExists exception) {
        log.error("Email already exists exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {PhoneAlreadyExists.class})
    public ResponseEntity<ErrorResponse> handlerPhoneAlreadyExistsEx(PhoneAlreadyExists exception) {
        log.error("Phone already exists exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UserAndTypeAlreadyExists.class})
    public ResponseEntity<ErrorResponse> handlerUserAndTypeAlreadyExistsEx(UserAndTypeAlreadyExists exception) {
        log.error("User and Type already exists exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UserNotFoundInDataBase.class})
    public ResponseEntity<ErrorResponse> handlerUserNotFoundInDataBaseEx(UserNotFoundInDataBase exception) {
        log.error("User not found in data base: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ErrorValidateCode.class})
    public ResponseEntity<ErrorResponse> handlerErrorSendEmailEx(ErrorValidateCode exception) {
        log.error("Error validate code: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {ErrorUpdateUserKeycloak.class})
    public ResponseEntity<ErrorResponse> handlerErrorUpdateUserKeycloak(ErrorUpdateUserKeycloak exception) {
        log.error("Error update user keycloak: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {ErrorContainDataPersonal.class})
    public ResponseEntity<ErrorResponse> handlerErrorContainDataPersonal(ErrorContainDataPersonal ex) {
        log.error("Error password not contain data personal: {}", ex.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getError().getType(), ex.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ErrorCreateSequence.class})
    public ResponseEntity<ErrorResponse> handlerErrorCreateSequence(ErrorCreateSequence exception) {

        log.error("Error create sequence: {}", exception.getError().getMessage());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {LoginException.class})
    protected ResponseEntity<ErrorResponse> handlerLoginEx(final LoginException ex) {
        log.error("Error in login: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getError().getType()), ex.getHttpStatus());
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        MessageResponse response = new MessageResponse();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        response.setCodeStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.setMessage("Validation error");
        response.setObject(errors);
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {ErrorCodeValidationExpired.class})
    public ResponseEntity<ErrorResponse> handlerErrorCodeValidationExpired(ErrorCodeValidationExpired ex) {

        log.error("Error code validation expired: {}", ex.getMessage());

        return new ResponseEntity<>(new ErrorResponse(ex.getType(), ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {LoginAttemptsError.class})
    public ResponseEntity<ErrorResponse> handlerNoFoundInRegistryEx(LoginAttemptsError exception) {
        log.error("Attempts exaggerated: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ErrorCreateUserKeycloak.class})
    public ResponseEntity<ErrorResponse> handlerErrorCreateUserKeycloak(ErrorCreateUserKeycloak exception) {

        log.error(LOG_ERROR_CREATE_USER, exception.getError().getMessage());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {ErrorFindCard.class})
    public ResponseEntity<ErrorResponse> handlerErrorFindCard(ErrorFindCard exception) {

        log.error(LOG_ERROR_CREATE_USER, exception.getError().getMessage());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(value = {DescriptionNotFound.class})
    public ResponseEntity<ErrorResponse> handlerDescriptionNotFoundEx(DescriptionNotFound exception) {
        log.error("Description not found: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(value = {CodeCIIUNotFound.class})
    public ResponseEntity<ErrorResponse> handlerCodeCIIUNotFoundEx(CodeCIIUNotFound exception) {
        log.error("Code CIIU not found: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(value = {CodeCIIUShorterLength.class})
    public ResponseEntity<ErrorResponse> handlerCodeCIIUShorterLengthEx(CodeCIIUShorterLength exception) {
        log.error("Code shorter length: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {CodeAndDescriptionException.class})
    public ResponseEntity<ErrorResponse> handlerCodeAndDescriptionNullEx(CodeAndDescriptionException exception) {
        log.error(LOG_ERROR_CODE_AND_DESCRIPTION_NULL, exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {AffiliationError.class})
    public ResponseEntity<ErrorResponse> handlerAffiliationError(AffiliationError exception) {
        log.error(LOG_ERROR_CODE_AND_DESCRIPTION_NULL, exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(value = {ErrorFindDocumentsAlfresco.class})
    public ResponseEntity<ErrorResponse> handlerErrorFindDocumentsAlfresco(ErrorFindDocumentsAlfresco exception) {
        log.error(LOG_ERROR_CODE_AND_DESCRIPTION_NULL, exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(value = {ActivityMaxSizeException.class})
    public ResponseEntity<ErrorResponse> handlerMaximumActivitiesAllowedEx(ActivityMaxSizeException exception) {
        log.error("Maximum activities allowed: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ErrorCreateResourceKeycloak.class})
    public ResponseEntity<ErrorResponse> handlerErrorCreateResourceKeycloak(ErrorCreateResourceKeycloak exception) {

        log.error(LOG_ERROR_CREATE_USER, exception.getError().getMessage());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {ErrorAssignmentResourceKeycloak.class})
    public ResponseEntity<ErrorResponse> handlerErrorAssignmentResourceKeycloak(ErrorAssignmentResourceKeycloak exception) {

        log.error(LOG_ERROR_CREATE_USER, exception.getError().getMessage());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {ErrorGetResourceKeycloak.class})
    public ResponseEntity<ErrorResponse> handlerErrorGetResourceKeycloak(ErrorGetResourceKeycloak exception) {

        log.error(LOG_ERROR_CREATE_USER, exception.getError().getMessage());

        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }
    @ExceptionHandler(value = {CancelAffiliationNotFoundException.class})
    public ResponseEntity<ErrorResponse> cancelAffiliationNotFoundException(CancelAffiliationNotFoundException exception) {
        log.error("Affiliation not found in the system: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }
    @ExceptionHandler(value = {DateCancelAffiliationException.class})
    public ResponseEntity<ErrorResponse> dateCancelAffiliationException(DateCancelAffiliationException exception) {
        log.error("Date affiliation last 24 hours: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {AffiliationAlreadyExistsError.class})
    public ResponseEntity<ErrorResponse> handlerAffiliationExistError(AffiliationAlreadyExistsError exception) {
        log.error("Affiliation already exists: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {AffiliationNotFoundError.class})
    public ResponseEntity<ErrorResponse> handlerAffiliationNotFoundError(AffiliationNotFoundError exception) {
        log.error("Affiliation not found: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType()),
                exception.getHttpStatus());
    }


    @ExceptionHandler(value = {ErrorGeneratedCard.class})
    public ResponseEntity<ErrorResponse> handlerErrorGeneratedCard(ErrorGeneratedCard exception) {
        log.error("Error generation card: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(value = {LastUpdatedException.class})
    public ResponseEntity<ErrorResponse> handlerErrorGeneratedCard(LastUpdatedException exception) {
        log.error("Last update data: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {WSConsultIndependentWorkerFound.class})
    public ResponseEntity<ErrorResponse> handlerErrorGeneratedCard(WSConsultIndependentWorkerFound exception) {
        log.error("WS consult response: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {ResponseMessageAffiliation.class})
    public ResponseEntity<ErrorResponse> responseMessageAffiliation(ResponseMessageAffiliation exception) {
        log.error("Response message affiliation: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.OK);
    }
    @ExceptionHandler(value = {ErrorAffiliationProvisionService.class})
    public ResponseEntity<ErrorResponse> responseErrorMessageAffiliation(ErrorAffiliationProvisionService exception) {
        log.error("Error in affiliation: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {IBCException.class})
    public ResponseEntity<ErrorResponse> handlerSmlmvError(IBCException exception) {
        log.error("Base income invalid: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {IndependenteFormException.class})
    public ResponseEntity<ErrorResponse> handlerIndependenteFormError(IndependenteFormException exception) {
        log.error("Error generating independent error: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {TypeEmployerDocumentRequested.class})
    public ResponseEntity<ErrorResponse> handlerTypeEmployerDocumentRequested(TypeEmployerDocumentRequested exception){
        log.error("Error, not found: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {WorkerRetirementException.class})
    public ResponseEntity<ErrorResponse> workerRetirementException(WorkerRetirementException exception) {
        log.error("Affiliation not found in the system: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {WorkspaceOptionOfficialException.class})
    public ResponseEntity<ErrorResponse> handlerErrorWorkspaceOptionOfficial(WorkspaceOptionOfficialException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Exception> handleGeneralException(Exception exception) {
        return new ResponseEntity<>(new Exception(exception.getMessage(), exception.getCause()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GeneralNoveltyException.class)
    public ResponseEntity<Error> handleGeneralNoveltyException(GeneralNoveltyException ex) {
        return new ResponseEntity<>(ex.getError(), ex.getHttpStatus());
    }

    @ExceptionHandler(DuplicateSessionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSessionException(DuplicateSessionException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {ErrorNumberAttemptsExceeded.class})
    public ResponseEntity<ErrorResponse> handlerNumbersAttemptsExceeded(ErrorNumberAttemptsExceeded exception) {
        log.error("Number attempts exceeded: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {FamilyMemeberError.class})
    public ResponseEntity<ErrorResponse> familyMemeberError(FamilyMemeberError exception) {
        log.error("Error: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {ErrorDocumentType.class})
    public ResponseEntity<ErrorResponse> handlerDocumentTypeEx(ErrorDocumentType exception) {
        log.error("Invalid document type exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ErrorDocumentConditions.class})
    public ResponseEntity<ErrorResponse> handlerDocumentConditionsEx(ErrorDocumentConditions exception) {
        log.error("Invalid document conditions exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ErrorExpirationTemporalPass.class})
    public ResponseEntity<ErrorResponse> handlerExpirationTemporalPasswordEx(ErrorExpirationTemporalPass exception) {
        log.error("Temporal password expired exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {NoveltyException.class})
    public ResponseEntity<ErrorResponse> handlerNoveltyException(NoveltyException exception) {
        log.error("Novelty exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {AffiliateNotFoundException.class})
    public ResponseEntity<ErrorResponse> handlerNoveltyException(AffiliateNotFoundException exception) {
        log.error("Affiliation exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<ErrorResponse> handlerNoveltyException(NotFoundException exception) {
        log.error("Register exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {PolicyException.class})
    public ResponseEntity<ErrorResponse> handlerPolicyException(PolicyException exception) {
        log.error("Policy exception: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {PasswordExpiredException.class})
    public ResponseEntity<ErrorResponse> handlerPasswordExpiredException(PasswordExpiredException exception) {
        log.error("PasswordExpiredException: {}", exception.getError().getMessage());
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {SatUpstreamError.class})
    public ResponseEntity<ErrorResponse> handlerSatUpstreamError(SatUpstreamError exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

    @ExceptionHandler(value = {SatError.class})
    public ResponseEntity<ErrorResponse> handlerSatError(SatError exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getError().getType(), exception.getError().getMessage()),
                exception.getHttpStatus());
    }

}
