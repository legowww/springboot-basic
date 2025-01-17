package com.dev.voucherproject.model.storage.voucher;

import com.dev.voucherproject.model.storage.io.CsvFileReader;
import com.dev.voucherproject.model.voucher.Voucher;
import com.dev.voucherproject.model.storage.io.VoucherFileWriter;
import com.dev.voucherproject.model.voucher.VoucherPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;


import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("csv")
public class CsvFileVoucherStorage implements VoucherStorage {

    @Value("${voucher.path}")
    private String path;

    @Value("${voucher.filename}")
    private String filename;

    private final CsvFileReader csvFileReader;

    private final VoucherFileWriter voucherFileWriter;

    public CsvFileVoucherStorage(CsvFileReader csvFileReader, VoucherFileWriter voucherFileWriter) {
        this.csvFileReader = csvFileReader;
        this.voucherFileWriter = voucherFileWriter;
    }

    @Override
    public void insert(Voucher voucher) {
        voucherFileWriter.write(voucher);
    }

    @Override
    public Optional<Voucher> findById(UUID voucherId) {
        Optional<String> lineByWord = csvFileReader.findLineByWord(voucherId.toString(), path, filename);

        if (lineByWord.isPresent()) {
            String line = lineByWord.get();
            Voucher findVoucher = csvFileParse(line);

            return Optional.of(findVoucher);
        }

        return Optional.empty();
    }

    @Override
    public List<Voucher> findAll() {
        return csvFileReader.readAllLines(path, filename)
                .stream()
                .map(this::csvFileParse)
                .toList();
    }

    public void deleteAll() {
        voucherFileWriter.clear();
    }

    private Voucher csvFileParse(final String line) {
        String[] data = line.split(",");

        try {
            return Voucher.of(UUID.fromString(data[2]), VoucherPolicy.valueOf(data[0]), Long.parseLong(data[1]));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format("{0} 파일은 잘못된 형식으로 작성되어 있습니다.", filename));
        }
    }
}
