import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    UTXOPool  utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool=new UTXOPool( utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
		//utxoPool.
        // IMPLEMENT THIS
		UTXOPool validTxs = new UTXOPool();
		double inputTotal = 0, outputTotal = 0;
        int i=0;
		for (Transaction.Input input: tx.getInputs()) {
			UTXO utxo                       = new UTXO(input.prevTxHash, input.outputIndex);
			Transaction.Output output   = this.utxoPool.getTxOutput(utxo);


			if (output == null) return false;

			if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i++), input.signature))
				return false;

			if (validTxs.contains(utxo)) return false;

			validTxs.addUTXO(utxo, output);
			inputTotal += output.value;
		}

		for (Transaction.Output output :tx.getOutputs() ){


			if (output.value < 0) return false;
			outputTotal += output.value;
		}


		return inputTotal >= outputTotal;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
		Set<Transaction> validTxs = new HashSet<>();

		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
				validTxs.add(tx);
				for (Transaction.Input in : tx.getInputs()) {
					UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
					utxoPool.removeUTXO(utxo);
				}
				int k=0;
				for (Transaction.Output out : tx.getOutputs() ){
					UTXO utxo = new UTXO(tx.getHash(), k++);
					utxoPool.addUTXO(utxo, out);
				}
			}
		}

		Transaction[] validTxArray = new Transaction[validTxs.size()];
		return validTxs.toArray(validTxArray);
    }

}
