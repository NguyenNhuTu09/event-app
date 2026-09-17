package com.example.backend.Service.Listener;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.backend.Service.AccountEmailService;
import com.example.backend.Service.CloudinaryService;
import com.example.backend.Service.ServiceImpl.EventMomentServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountDeletionListener {

    private final EventMomentServiceImpl momentService;
    private final CloudinaryService cloudinaryService;
    private final MediaReferenceChecker mediaReferenceChecker;
    private final AccountEmailService accountEmailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountDeleted(AccountDeletedEvent event) {
        broadcastMomentRemovals(event);
        deleteUnreferencedMedia(event);
        sendConfirmationEmail(event);
    }

    private void broadcastMomentRemovals(AccountDeletedEvent event) {
        for (AccountDeletedEvent.MomentRef ref : event.deletedMoments()) {
            try {
                momentService.publishMomentEvent(ref.eventId(), "DELETE", ref.momentId());
            } catch (Exception e) {
                log.error("ACCOUNT_DELETION ws-failed userId={} momentId={}: {}",
                        event.userId(), ref.momentId(), e.getMessage());
            }
        }
    }

    private void deleteUnreferencedMedia(AccountDeletedEvent event) {
        try {
            List<String> deletable = event.mediaUrlCandidates().stream()
                    .filter(url -> !mediaReferenceChecker.isStillReferenced(url))
                    .collect(Collectors.toList());

            int deleted = deletable.isEmpty() ? 0 : cloudinaryService.deleteByUrls(deletable);

            log.info("ACCOUNT_DELETION media userId={} candidates={} unreferenced={} deleted={}",
                    event.userId(), event.mediaUrlCandidates().size(), deletable.size(), deleted);
        } catch (Exception e) {
            log.error("ACCOUNT_DELETION media-failed userId={}: {}", event.userId(), e.getMessage());
        }
    }

    private void sendConfirmationEmail(AccountDeletedEvent event) {
        try {
            accountEmailService.sendAccountDeletedEmail(
                    event.originalEmail(), event.originalUsername(), event.deletedByAdmin());
        } catch (Exception e) {
            log.error("ACCOUNT_DELETION email-failed userId={}: {}", event.userId(), e.getMessage());
        }
    }
}