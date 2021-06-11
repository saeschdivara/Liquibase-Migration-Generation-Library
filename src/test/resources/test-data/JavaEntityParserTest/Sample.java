package foo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Sample {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bank_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Bank bank;

	@Column(length = 40, nullable = false, name = "sample_name")
	private String name;

	@Column(length = 15)
	private String accountNumber;
}