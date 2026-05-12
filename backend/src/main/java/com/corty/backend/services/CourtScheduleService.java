package com.corty.backend.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.corty.backend.dto.CourtBlockRequest;
import com.corty.backend.dto.CourtBlockResponse;
import com.corty.backend.dto.CourtScheduleRequest;
import com.corty.backend.dto.CourtScheduleResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Court;
import com.corty.backend.model.CourtBlock;
import com.corty.backend.model.CourtSchedule;
import com.corty.backend.repository.CourtBlockRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.CourtScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourtScheduleService {

    private final CourtRepository courtRepository;
    private final CourtScheduleRepository courtScheduleRepository;
    private final CourtBlockRepository courtBlockRepository;

    public List<CourtScheduleResponse> getSchedules(Long courtId) {
        findCourtOrThrow(courtId);
        List<CourtSchedule> schedules = courtScheduleRepository.findByCourtIdCourtOrderByDayOfWeek(courtId);
        return schedules.stream().map(this::toScheduleResponse).toList();
    }

    @Transactional
    public List<CourtScheduleResponse> replaceSchedules(Long courtId, List<CourtScheduleRequest> requests) {
        Court court = findCourtOrThrow(courtId);
        requests.stream()
                .filter(req -> !req.isClosed() && req.getOpenTime() != null && req.getCloseTime() != null)
                .filter(req -> !req.getOpenTime().isBefore(req.getCloseTime()))
                .findFirst()
                .ifPresent(req -> {
                    throw new BusinessLogicException(
                            "La hora de apertura debe ser anterior a la de cierre (" + req.getDayOfWeek() + ")"
                    );
                });
        courtScheduleRepository.deleteByCourtIdCourt(courtId);
        courtScheduleRepository.flush();
        List<CourtSchedule> saved = requests.stream()
                .map(req -> {
                    CourtSchedule schedule = CourtSchedule.builder()
                            .dayOfWeek(req.getDayOfWeek())
                            .openTime(req.getOpenTime())
                            .closeTime(req.getCloseTime())
                            .closed(req.isClosed())
                            .court(court)
                            .build();
                    return courtScheduleRepository.save(schedule);
                })
                .toList();
        return saved.stream().map(this::toScheduleResponse).toList();
    }

    @Transactional
    public void updateUseClubSchedule(Long courtId, boolean useClubSchedule) {
        Court court = findCourtOrThrow(courtId);
        court.setUseClubSchedule(useClubSchedule);
        courtRepository.save(court);
    }

    @Transactional
    public void updateSlotDuration(Long courtId, int slotDurationMinutes) {
        Court court = findCourtOrThrow(courtId);
        court.setSlotDurationMinutes(slotDurationMinutes);
        courtRepository.save(court);
    }

    public List<CourtBlockResponse> getBlocks(Long courtId) {
        findCourtOrThrow(courtId);
        List<CourtBlock> blocks = courtBlockRepository.findByCourtIdCourtOrderByBlockDateAscStartTimeAsc(courtId);
        return blocks.stream().map(this::toBlockResponse).toList();
    }

    @Transactional
    public CourtBlockResponse addBlock(Long courtId, CourtBlockRequest request) {
        if (request.getBlockDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException("No se pueden crear bloqueos en fechas pasadas");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessLogicException("La hora de inicio debe ser anterior a la hora de fin");
        }
        Court court = findCourtOrThrow(courtId);
        CourtBlock block = CourtBlock.builder()
                .blockDate(request.getBlockDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .court(court)
                .build();
        CourtBlock saved = courtBlockRepository.save(block);
        return toBlockResponse(saved);
    }

    @Transactional
    public void deleteBlock(Long courtId, Long blockId) {
        CourtBlock block = courtBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueo no encontrado"));
        if (!block.getCourt().getIdCourt().equals(courtId)) {
            throw new ResourceNotFoundException("Bloqueo no encontrado en esta pista");
        }
        courtBlockRepository.delete(block);
    }

    private Court findCourtOrThrow(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pista no encontrada"));
    }

    private CourtScheduleResponse toScheduleResponse(CourtSchedule schedule) {
        CourtScheduleResponse response = new CourtScheduleResponse();
        response.setId(schedule.getIdCourtSchedule());
        response.setDayOfWeek(schedule.getDayOfWeek());
        response.setOpenTime(schedule.getOpenTime());
        response.setCloseTime(schedule.getCloseTime());
        response.setClosed(schedule.isClosed());
        return response;
    }

    private CourtBlockResponse toBlockResponse(CourtBlock block) {
        CourtBlockResponse response = new CourtBlockResponse();
        response.setId(block.getIdCourtBlock());
        response.setBlockDate(block.getBlockDate());
        response.setStartTime(block.getStartTime());
        response.setEndTime(block.getEndTime());
        response.setReason(block.getReason());
        return response;
    }
}
