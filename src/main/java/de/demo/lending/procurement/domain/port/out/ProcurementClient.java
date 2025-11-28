package de.demo.lending.procurement.domain.port.out;

import de.demo.lending.procurement.adapters.out.external.OpenLibraryClientAdapter;

public interface ProcurementClient {
    OpenLibraryClientAdapter.ExternalBookInfo searchBook(String query);

    String orderBook(String externalBookId);
}
