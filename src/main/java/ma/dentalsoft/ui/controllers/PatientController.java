package ma.dentalsoft.ui.controllers;

import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.dto.AssuranceDTO;
import ma.dentalsoft.dto.PageResultatDTO;
import ma.dentalsoft.dto.PatientRow;
import ma.dentalsoft.exceptions.DatabaseException;
import ma.dentalsoft.exceptions.DentalSoftException;
import ma.dentalsoft.service.api.AssuranceService;
import ma.dentalsoft.service.api.PatientService;
import ma.dentalsoft.session.SessionManager;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur de l'écran Patients : exécute les appels de service dans un SwingWorker
 * (BNF-04) et rappelle la vue sur l'EDT avec le résultat ou un message d'erreur lisible.
 * Aucun SQL ici : tout passe par les services.
 */
public class PatientController {

    private static final Logger LOG = Logger.getLogger(PatientController.class.getName());
    static final String MESSAGE_BASE_INDISPONIBLE =
            "La base est momentanément indisponible. Rechargez les données avant de réessayer.";

    private final PatientService patientService;
    private final AssuranceService assuranceService;
    private final SessionManager session;

    public PatientController(PatientService patientService, AssuranceService assuranceService,
                             SessionManager session) {
        this.patientService = patientService;
        this.assuranceService = assuranceService;
        this.session = session;
    }

    public void chargerAssurances(Consumer<List<AssuranceDTO>> succes, Consumer<String> echec) {
        executer(assuranceService::listerAssurances, succes, echec);
    }

    public void rechercher(String motCle, int page, int taillePage,
                           Consumer<PageResultatDTO<PatientRow>> succes, Consumer<String> echec) {
        executer(() -> patientService.rechercherPaginer(motCle, page, taillePage), succes, echec);
    }

    /** Crée le patient et son dossier médical dans une seule transaction (service T04). */
    public void creerPatient(Patient patient, Consumer<Patient> succes, Consumer<String> echec) {
        var utilisateur = session.getUtilisateur();
        Long utilisateurId = utilisateur == null ? null : utilisateur.getIdUtilisateur();
        executer(() -> patientService.creerPatient(patient, utilisateurId), succes, echec);
    }

    /** Exécute la tâche hors EDT ; succes/echec sont toujours appelés sur l'EDT. */
    private <T> void executer(Callable<T> tache, Consumer<T> succes, Consumer<String> echec) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tache.call();
            }

            @Override
            protected void done() {
                try {
                    succes.accept(get());
                } catch (ExecutionException e) {
                    echec.accept(messageUtilisateur(e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** Message métier affichable tel quel ; détails techniques uniquement dans les logs (BNF-07). */
    static String messageUtilisateur(Throwable cause) {
        if (cause instanceof DentalSoftException && !(cause instanceof DatabaseException)) {
            return cause.getMessage();
        }
        LOG.log(Level.SEVERE, "Erreur technique dans l'écran Patients", cause);
        return MESSAGE_BASE_INDISPONIBLE;
    }
}
