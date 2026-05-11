package com.kiranastore.kirana.service;

import com.kiranastore.kirana.entity.History;
import com.kiranastore.kirana.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HistoryService {
    
    @Autowired
    private HistoryRepository historyRepository;
    
    public History saveHistory(Long userId, String tableData, String action) {
        History.Action actionEnum = History.Action.valueOf(action.toUpperCase());
        
        History history = new History();
        history.setUserId(userId);
        history.setTableData(tableData);
        history.setAction(actionEnum);
        history.setTimestamp(LocalDateTime.now());
        
        return historyRepository.save(history);
    }
    
    public List<History> getUserHistory(Long userId) {
        return historyRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    public Optional<History> getHistoryById(Long historyId) {
        return historyRepository.findById(historyId);
    }
    
    public Optional<History> getHistoryByIdAndUserId(Long historyId, Long userId) {
        return Optional.ofNullable(historyRepository.findByIdAndUserId(historyId, userId));
    }
    
    public boolean deleteHistory(Long historyId, Long userId) {
        Optional<History> history = getHistoryByIdAndUserId(historyId, userId);
        if (history.isPresent()) {
            historyRepository.delete(history.get());
            return true;
        }
        return false;
    }
}
