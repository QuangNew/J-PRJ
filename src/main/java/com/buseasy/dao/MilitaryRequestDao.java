package com.buseasy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.buseasy.config.DBConnection;
import com.buseasy.model.MilitaryRequest;

public class MilitaryRequestDao {

    public MilitaryRequest findById(int requestId) throws SQLException {
        String sql = selectRequestQuery() + "WHERE mr.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public MilitaryRequest findLatestByUserId(int userId) throws SQLException {
        String sql = selectRequestQuery()
                   + "WHERE mr.user_id = ? ORDER BY mr.created_at DESC, mr.id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public MilitaryRequest findPendingByUserId(int userId) throws SQLException {
        String sql = selectRequestQuery()
                   + "WHERE mr.user_id = ? AND mr.status = 'PENDING' "
                   + "ORDER BY mr.created_at DESC, mr.id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void insert(MilitaryRequest request) throws SQLException {
        String sql = "INSERT INTO military_requests (user_id, service_number, unit_name, note) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getServiceNumber());
            stmt.setString(3, request.getUnitName());
            stmt.setString(4, request.getNote());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    request.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<MilitaryRequest> findAll(String searchText, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(selectRequestQuery()).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        String query = searchText == null ? "" : searchText.trim().toLowerCase();
        if (!query.isBlank()) {
            String like = "%" + query + "%";
            sql.append("AND (LOWER(u.username) LIKE ? OR LOWER(u.full_name) LIKE ? ")
               .append("OR LOWER(mr.service_number) LIKE ? OR LOWER(mr.unit_name) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if ("PENDING".equalsIgnoreCase(statusFilter)
                || "APPROVED".equalsIgnoreCase(statusFilter)
                || "DENIED".equalsIgnoreCase(statusFilter)) {
            sql.append("AND mr.status = ? ");
            params.add(statusFilter.toUpperCase());
        }
        sql.append("ORDER BY FIELD(mr.status, 'PENDING', 'APPROVED', 'DENIED'), mr.created_at DESC");

        List<MilitaryRequest> requests = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRow(rs));
                }
            }
        }
        return requests;
    }

    public void review(int requestId, int adminId, String status, String adminNote) throws SQLException {
        String sql = "UPDATE military_requests "
                   + "SET status = ?, admin_note = ?, reviewed_by = ?, reviewed_at = NOW() "
                   + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, adminNote);
            stmt.setInt(3, adminId);
            stmt.setInt(4, requestId);
            stmt.executeUpdate();
        }
    }

    private String selectRequestQuery() {
        return "SELECT mr.*, u.username, u.full_name "
             + "FROM military_requests mr "
             + "JOIN users u ON mr.user_id = u.id ";
    }

    private void bindParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setString(i + 1, String.valueOf(params.get(i)));
        }
    }

    private MilitaryRequest mapRow(ResultSet rs) throws SQLException {
        MilitaryRequest request = new MilitaryRequest();
        request.setId(rs.getInt("id"));
        request.setUserId(rs.getInt("user_id"));
        request.setUsername(rs.getString("username"));
        request.setFullName(rs.getString("full_name"));
        request.setServiceNumber(rs.getString("service_number"));
        request.setUnitName(rs.getString("unit_name"));
        request.setNote(rs.getString("note"));
        request.setStatus(rs.getString("status"));
        request.setAdminNote(rs.getString("admin_note"));
        int reviewedBy = rs.getInt("reviewed_by");
        request.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            request.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp reviewed = rs.getTimestamp("reviewed_at");
        if (reviewed != null) {
            request.setReviewedAt(reviewed.toLocalDateTime());
        }
        return request;
    }
}
